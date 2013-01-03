/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2010 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connectbot.service;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.text.ClipboardManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import org.connectbot.transport.AbsTransport;
import org.connectbot.util.PreferenceConstants;
import org.connectbot.util.SelectionArea;

import com.e3roid.util.Debug;

import java.io.IOException;

/**
 * @author kenny
 * @author modified by raaar
 */
public class TerminalKeyListener implements OnKeyListener, OnSharedPreferenceChangeListener {

  public final static int META_CTRL_ON = 0x01;
  public final static int META_CTRL_LOCK = 0x02;
  public final static int META_ALT_ON = 0x04;
  public final static int META_ALT_LOCK = 0x08;
  public final static int META_SHIFT_ON = 0x10;
  public final static int META_SHIFT_LOCK = 0x20;
  public final static int META_SLASH = 0x40;
  public final static int META_TAB = 0x80;

  // The bit mask of momentary and lock states for each
  public final static int META_CTRL_MASK = META_CTRL_ON | META_CTRL_LOCK;
  public final static int META_ALT_MASK = META_ALT_ON | META_ALT_LOCK;
  public final static int META_SHIFT_MASK = META_SHIFT_ON | META_SHIFT_LOCK;

  // All the transient key codes
  public final static int META_TRANSIENT = META_CTRL_ON | META_ALT_ON | META_SHIFT_ON;

  private final TerminalManager manager;
  private final TerminalBridge bridge;
  private final TerminalBuffer buffer;

  protected KeyCharacterMap keymap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);

  private String keymode = null;
  private boolean hardKeyboard = false;

  private int metaState = 0;

  private ClipboardManager clipboard = null;
  private boolean selectingForCopy = false;
  private final SelectionArea selectionArea;

  private String encoding;

  public TerminalKeyListener(TerminalManager manager, TerminalBridge bridge, TerminalBuffer buffer,
      String encoding) {
    this.manager = manager;
    this.bridge = bridge;
    this.buffer = buffer;
    this.encoding = encoding;

    selectionArea = new SelectionArea();

    manager.registerOnSharedPreferenceChangeListener(this);

    hardKeyboard =
        (manager.getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY);

    updateKeymode();
  }

  /**
   * Handle onKey() events coming down from a {@link TerminalView} above us. Modify the keys to make
   * more sense to a host then pass it to the transport.
   */
  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    try {
      final boolean hardKeyboardHidden = manager.isHardKeyboardHidden();

      AbsTransport transport = bridge.getTransport();

      // Ignore all key-up events except for the special keys
      if (event.getAction() == KeyEvent.ACTION_UP) {
        // There's nothing here for virtual keyboard users.
        if (!hardKeyboard || (hardKeyboard && hardKeyboardHidden)) {
          return false;
        }

        // skip keys if we aren't connected yet or have been disconnected
        if (transport == null || !transport.isSessionOpen()) {
          return false;
        }

        if (PreferenceConstants.KEYMODE_RIGHT.equals(keymode)) {
          if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT && (metaState & META_SLASH) != 0) {
            metaState &= ~(META_SLASH | META_TRANSIENT);
            transport.write('/');
            return true;
          } else if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT && (metaState & META_TAB) != 0) {
            metaState &= ~(META_TAB | META_TRANSIENT);
            transport.write(0x09);
            return true;
          }
        } else if (PreferenceConstants.KEYMODE_LEFT.equals(keymode)) {
          if (keyCode == KeyEvent.KEYCODE_ALT_LEFT && (metaState & META_SLASH) != 0) {
            metaState &= ~(META_SLASH | META_TRANSIENT);
            transport.write('/');
            return true;
          } else if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT && (metaState & META_TAB) != 0) {
            metaState &= ~(META_TAB | META_TRANSIENT);
            transport.write(0x09);
            return true;
          }
        }

        return false;
      }

      if (keyCode == KeyEvent.KEYCODE_BACK && transport != null) {
        bridge.dispatchDisconnect(!transport.isSessionOpen());
        return true;
      }

      // check for terminal resizing keys
      if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
        bridge.increaseFontSize();
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        bridge.decreaseFontSize();
        return true;
      }

      // skip keys if we aren't connected yet or have been disconnected
      if (transport == null || !transport.isSessionOpen()) {
        return false;
      }
      
      boolean printing = (keymap.isPrintingKey(keyCode) || keyCode == KeyEvent.KEYCODE_SPACE);

      // otherwise pass through to existing session
      // print normal keys
      if (printing) {
        int curMetaState = event.getMetaState();

        metaState &= ~(META_SLASH | META_TAB);

        if ((metaState & META_SHIFT_MASK) != 0) {
          curMetaState |= KeyEvent.META_SHIFT_ON;
          metaState &= ~META_SHIFT_ON;
          bridge.redraw();
        }

        if ((metaState & META_ALT_MASK) != 0) {
          curMetaState |= KeyEvent.META_ALT_ON;
          metaState &= ~META_ALT_ON;
          bridge.redraw();
        }

        int key = keymap.get(keyCode, curMetaState);

        if ((metaState & META_CTRL_MASK) != 0) {
          metaState &= ~META_CTRL_ON;
          bridge.redraw();

          if ((!hardKeyboard || (hardKeyboard && hardKeyboardHidden)) && sendFunctionKey(keyCode)) {
            return true;
          }

          // Support CTRL-a through CTRL-z
          if (key >= 0x61 && key <= 0x7A) {
            key -= 0x60;
          } else if (key >= 0x41 && key <= 0x5F) {
            key -= 0x40;
          } else if (key == 0x20) {
            key = 0x00;
          } else if (key == 0x3F) {
            key = 0x7F;
          }
        }

        // handle pressing f-keys
        if ((hardKeyboard && !hardKeyboardHidden) && (curMetaState & KeyEvent.META_SHIFT_ON) != 0
            && sendFunctionKey(keyCode)) {
          return true;
        }

        if (key < 0x80) {
          transport.write(key);
        } else {
          // TODO write encoding routine that doesn't allocate each time
          transport.write(new String(Character.toChars(key)).getBytes(encoding));
        }

        return true;
      }

      if (keyCode == KeyEvent.KEYCODE_UNKNOWN && event.getAction() == KeyEvent.ACTION_MULTIPLE) {
        byte[] input = event.getCharacters().getBytes(encoding);
        transport.write(input);
        return true;
      }

      // try handling keymode shortcuts
      if (hardKeyboard && !hardKeyboardHidden && event.getRepeatCount() == 0) {
        if (PreferenceConstants.KEYMODE_RIGHT.equals(keymode)) {
          switch (keyCode) {
          case KeyEvent.KEYCODE_ALT_RIGHT:
            metaState |= META_SLASH;
            return true;
          case KeyEvent.KEYCODE_SHIFT_RIGHT:
            metaState |= META_TAB;
            return true;
          case KeyEvent.KEYCODE_SHIFT_LEFT:
            metaPress(META_SHIFT_ON);
            return true;
          case KeyEvent.KEYCODE_ALT_LEFT:
            metaPress(META_ALT_ON);
            return true;
          }
        } else if (PreferenceConstants.KEYMODE_LEFT.equals(keymode)) {
          switch (keyCode) {
          case KeyEvent.KEYCODE_ALT_LEFT:
            metaState |= META_SLASH;
            return true;
          case KeyEvent.KEYCODE_SHIFT_LEFT:
            metaState |= META_TAB;
            return true;
          case KeyEvent.KEYCODE_SHIFT_RIGHT:
            metaPress(META_SHIFT_ON);
            return true;
          case KeyEvent.KEYCODE_ALT_RIGHT:
            metaPress(META_ALT_ON);
            return true;
          }
        } else {
          switch (keyCode) {
          case KeyEvent.KEYCODE_ALT_LEFT:
          case KeyEvent.KEYCODE_ALT_RIGHT:
            metaPress(META_ALT_ON);
            return true;
          case KeyEvent.KEYCODE_SHIFT_LEFT:
          case KeyEvent.KEYCODE_SHIFT_RIGHT:
            metaPress(META_SHIFT_ON);
            return true;
          }
        }
      }

      // look for special chars
      switch (keyCode) {
      case KeyEvent.KEYCODE_CAMERA:

        // check to see which shortcut the camera button triggers
        String camera =
            manager.getStringParameter(PreferenceConstants.CAMERA,
                PreferenceConstants.CAMERA_CTRLA_SPACE);
        if (PreferenceConstants.CAMERA_CTRLA_SPACE.equals(camera)) {
          transport.write(0x01);
          transport.write(' ');
        } else if (PreferenceConstants.CAMERA_CTRLA.equals(camera)) {
          transport.write(0x01);
        } else if (PreferenceConstants.CAMERA_ESC.equals(camera)) {
          buffer.keyTyped(TerminalBuffer.KEY_ESCAPE, ' ', 0);
        } else if (PreferenceConstants.CAMERA_ESC_A.equals(camera)) {
          buffer.keyTyped(TerminalBuffer.KEY_ESCAPE, ' ', 0);
          transport.write('a');
        }

        break;

      case KeyEvent.KEYCODE_DEL:
        buffer.keyPressed(TerminalBuffer.KEY_BACK_SPACE, ' ', getStateForBuffer());
        metaState &= ~META_TRANSIENT;
        return true;
      case KeyEvent.KEYCODE_ENTER:
        buffer.keyTyped(TerminalBuffer.KEY_ENTER, ' ', 0);
        metaState &= ~META_TRANSIENT;
        return true;

      case KeyEvent.KEYCODE_DPAD_LEFT:
        if (selectingForCopy) {
          selectionArea.decrementColumn();
          bridge.redraw();
        } else {
          buffer.keyPressed(TerminalBuffer.KEY_LEFT, ' ', getStateForBuffer());
          metaState &= ~META_TRANSIENT;
        }
        return true;

      case KeyEvent.KEYCODE_DPAD_UP:
        if (selectingForCopy) {
          selectionArea.decrementRow();
          bridge.redraw();
        } else {
          buffer.keyPressed(TerminalBuffer.KEY_UP, ' ', getStateForBuffer());
          metaState &= ~META_TRANSIENT;
        }
        return true;

      case KeyEvent.KEYCODE_DPAD_DOWN:
        if (selectingForCopy) {
          selectionArea.incrementRow();
          bridge.redraw();
        } else {
          buffer.keyPressed(TerminalBuffer.KEY_DOWN, ' ', getStateForBuffer());
          metaState &= ~META_TRANSIENT;
        }
        return true;

      case KeyEvent.KEYCODE_DPAD_RIGHT:
        if (selectingForCopy) {
          selectionArea.incrementColumn();
          bridge.redraw();
        } else {
          buffer.keyPressed(TerminalBuffer.KEY_RIGHT, ' ', getStateForBuffer());
          metaState &= ~META_TRANSIENT;
        }
        return true;

      case KeyEvent.KEYCODE_DPAD_CENTER:
        if (selectingForCopy) {
          if (selectionArea.isSelectingOrigin()) {
            selectionArea.finishSelectingOrigin();
          } else {
            if (clipboard != null) {
              // copy selected area to clipboard
              String copiedText = selectionArea.copyFrom(buffer);

              clipboard.setText(copiedText);
              // XXX STOPSHIP
              // manager.notifyUser(manager.getString(
              // R.string.console_copy_done,
              // copiedText.length()));

              selectingForCopy = false;
              selectionArea.reset();
            }
          }
        } else {
          if ((metaState & META_CTRL_ON) != 0) {
            buffer.keyTyped(TerminalBuffer.KEY_ESCAPE, ' ', 0);
            metaState &= ~META_CTRL_ON;
          } else {
            metaState |= META_CTRL_ON;
          }
        }

        bridge.redraw();

        return true;
      }

    } catch (IOException e) {
      Debug.e("Problem while trying to handle an onKey() event", e, false);
      try {
        bridge.getTransport().flush();
      } catch (IOException ioe) {
        Debug.d("Our transport was closed, dispatching disconnect event", false);
        bridge.dispatchDisconnect(false);
      }
    } catch (NullPointerException npe) {
      Debug.d("Input before connection established ignored.", false);
      return true;
    }

    return false;
  }

  /**
   * @param keyCode
   * @return successful
   */
  private boolean sendFunctionKey(int keyCode) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_1:
      buffer.keyPressed(TerminalBuffer.KEY_F1, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_2:
      buffer.keyPressed(TerminalBuffer.KEY_F2, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_3:
      buffer.keyPressed(TerminalBuffer.KEY_F3, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_4:
      buffer.keyPressed(TerminalBuffer.KEY_F4, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_5:
      buffer.keyPressed(TerminalBuffer.KEY_F5, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_6:
      buffer.keyPressed(TerminalBuffer.KEY_F6, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_7:
      buffer.keyPressed(TerminalBuffer.KEY_F7, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_8:
      buffer.keyPressed(TerminalBuffer.KEY_F8, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_9:
      buffer.keyPressed(TerminalBuffer.KEY_F9, ' ', 0);
      return true;
    case KeyEvent.KEYCODE_0:
      buffer.keyPressed(TerminalBuffer.KEY_F10, ' ', 0);
      return true;
    default:
      return false;
    }
  }

  /**
   * Handle meta key presses where the key can be locked on.
   * <p>
   * 1st press: next key to have meta state<br />
   * 2nd press: meta state is locked on<br />
   * 3rd press: disable meta state
   * 
   * @param code
   */
  private void metaPress(int code) {
    if ((metaState & (code << 1)) != 0) {
      metaState &= ~(code << 1);
    } else if ((metaState & code) != 0) {
      metaState &= ~code;
      metaState |= code << 1;
    } else {
      metaState |= code;
    }
    bridge.redraw();
  }

  public void setTerminalKeyMode(String keymode) {
    this.keymode = keymode;
  }

  private int getStateForBuffer() {
    int bufferState = 0;

    if ((metaState & META_CTRL_MASK) != 0) {
      bufferState |= TerminalBuffer.KEY_CONTROL;
    }
    if ((metaState & META_SHIFT_MASK) != 0) {
      bufferState |= TerminalBuffer.KEY_SHIFT;
    }
    if ((metaState & META_ALT_MASK) != 0) {
      bufferState |= TerminalBuffer.KEY_ALT;
    }

    return bufferState;
  }

  public int getMetaState() {
    return metaState;
  }

  public void setClipboardManager(ClipboardManager clipboard) {
    this.clipboard = clipboard;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PreferenceConstants.KEYMODE.equals(key)) {
      updateKeymode();
    }
  }

  private void updateKeymode() {
    keymode =
        manager.getStringParameter(PreferenceConstants.KEYMODE, PreferenceConstants.KEYMODE_RIGHT);
  }

  public void setCharset(String encoding) {
    this.encoding = encoding;
  }

  public boolean onCommitText(View view, CharSequence text, int newCursorPosition) {
	  return onKey(view, KeyEvent.KEYCODE_UNKNOWN, 
			  new KeyEvent(SystemClock.uptimeMillis(), 
					  text.toString(), 0, KeyEvent.FLAG_SOFT_KEYBOARD));
  }
}
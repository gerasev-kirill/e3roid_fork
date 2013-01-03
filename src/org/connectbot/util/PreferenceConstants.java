/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.util;

/**
 * @author Kenny Root
 * @author modified by raaar
 */
public class PreferenceConstants {

  public static final String SCROLLBACK = "terminal_scrollback";
  public static final String FONTSIZE = "terminal_fontsize";
  public static final String KEYMODE = "terminal_keymode";
  public static final String ENCODING = "terminal_encoding";

  public static final String DELKEY = "terminal_delkey";
  public static final String DELKEY_BACKSPACE = "terminal_backspace";
  public static final String DELKEY_DEL = "terminal_del";

  public static final String KEYMODE_RIGHT = "Use right-side keys";
  public static final String KEYMODE_LEFT = "Use left-side keys";

  public static final String CAMERA = "terminal_camera";

  public static final String CAMERA_CTRLA_SPACE = "Ctrl+A then Space";
  public static final String CAMERA_CTRLA = "Ctrl+A";
  public static final String CAMERA_ESC = "Esc";
  public static final String CAMERA_ESC_A = "Esc+A";

  public final static int DEFAULT_FG_COLOR = 0xffcccccc;
  public final static int DEFAULT_BG_COLOR = 0xff000000;
  public final static int DEFAULT_SCROLLBACK = 140;
  public final static float DEFAULT_FONT_SIZE = 10;

  public final static String COLOR_FG = "terminal_color_fg";
  public final static String COLOR_BG = "terminal_color_bg";

}

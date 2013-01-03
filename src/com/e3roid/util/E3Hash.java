package com.e3roid.util;

import java.util.Hashtable;
import java.io.*;

import android.content.Context;
import android.content.res.AssetManager;
/**
 * @author kirill
 *
 */
public class E3Hash extends Hashtable{
	private Hashtable htTemp;
	public E3Hash(String fileName, Context context){
		super();
		   try {
		    	AssetManager am = context.getAssets();
		        BufferedReader in = new BufferedReader(new InputStreamReader(am.open(fileName)));
		        String str = "";
		        while ((str = in.readLine()) != null) {
						this.parseString(str);
						this.put(this.getName(str), htTemp);
		        }
		      } 
		    catch (IOException e) {
		      }
}
	
	private  String getName(String strTmp){
		String name=strTmp.substring(0, strTmp.indexOf("=")-1);
		return name;
	}
	
	private void parseString(String strTmp){
		htTemp=new Hashtable();
		htTemp.put("metaName", strTmp.substring(0, strTmp.indexOf("=")-1));
		String base=strTmp.substring(strTmp.indexOf("{")+1,strTmp.length()-1);
		Integer i = base.indexOf(",");
		if (i==-1){
			i=base.length();
		}
		String tmp;
		Integer j=new Integer(0);
		while (j.equals(base.length()+1)==false){
				tmp=base.substring(j,i);
				int f=tmp.indexOf(":");
				int tm=tmp.indexOf(";");
				if (tm!=-1){
					Integer[] v1;
					v1=new Integer[2];
					String t1;
					t1=tmp.substring(f+2,tm);
					v1[0]=Integer.parseInt(t1);
					t1=tmp.substring(tm+1,tmp.length()-1);
					v1[1]=Integer.parseInt(t1);
					htTemp.put(tmp.substring(0,f), v1);
				}
				else{
					htTemp.put(tmp.substring(0,f), tmp.substring(f+1));
				}
				j=i+1;
				i=base.indexOf(",",j);
				if (i.equals(-1)){
						i=base.length();
				}
		}			
	}	
}

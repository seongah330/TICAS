/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.umn.natsrl.infra;

import edu.umn.natsrl.infra.types.InfraType;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


/**
 *
 * @author Chongmyung Park
 */
public abstract class InfraObject implements Serializable {
    
    protected String id;
    protected Properties property = new Properties();
    protected InfraType infraType;
   
    public InfraObject() {
        
    }

    public InfraObject(Element element) {
        this();
        NamedNodeMap attrs = element.getAttributes();
        for(int i=0; i<attrs.getLength(); i++)
        {
            String attr_name = attrs.item(i).getNodeName();
            String attr_value = attrs.item(i).getNodeValue();
            this.property.setProperty(attr_name, attr_value);            
        }        
    }

    public InfraObject(Properties property)
    {
        this();
        this.property = property;
    }

    protected void setProperty(Object key, Object value)
    {
        this.property.setProperty(key.toString(), value.toString());
    }

    protected String getProperty(InfraProperty prop) {
        return getProperty(prop.toString());
    }
    
    protected int getPropertyInt(InfraProperty prop) {
        return this.getPropertyInt(prop.toString());
    }   
    
    protected String[] getPropertyArray(InfraProperty prop) {
        return getPropertyArray(prop.toString());
    }       
    
    protected double getPropertyDouble(InfraProperty prop) {
        String p = this.property.getProperty(prop.toString());
        if(p == null || p.isEmpty()) return -1;
        return Double.parseDouble(p);
    }    
    
    protected String getProperty(String key)
    {
        return this.property.getProperty(key);
    }

    protected String[] getPropertyArray(String key)
    {
        String v = this.property.getProperty(key);
        if(v == null || v.isEmpty()) return null;
        return v.split(" ");
    }

    protected boolean isPropertyArray(String key)
    {
        String v = this.property.getProperty(key);
        if(v == null || !v.contains(" ")) return false;
        else return true;
    }

    protected int getPropertyInt(String key)
    {
        String v = this.property.getProperty(key);
        if(v == null || v.isEmpty()) return -1;
        else return Integer.parseInt(v);
    }

    protected double getPropertyDouble(String key)
    {
        String v = this.property.getProperty(key);
        if(v == null || v.isEmpty()) return -1;
        else return Double.parseDouble(v);
    }


    protected String arrayToCSV(String[] str) {
        StringBuilder sb = new StringBuilder();
        int last = str.length-1;
        for(int i=0; i<str.length; i++)
        {
            sb.append(str[i]);
            if(i != last) sb.append(",");
        }
        return sb.toString();
    }

    protected String[] csvToArray(String csv){
        String[] splittArray = null;
        if (csv != null || !csv.equalsIgnoreCase("")){
             splittArray = csv.split(",");
        }
        return splittArray;
    }

    public void setId(String _id){
        id = _id;
    }
    public String getId() {
        if(id == null)
            return "null";
        return this.id;
    }
    
    protected TMO getTMO() {
        return TMO.getInstance();
    }
    
    
    protected double[] toDoubleArray(Vector<Double> data)
    {        
        if(data == null) return null;        
        double[] ret = new double[data.size()];
        for(int i=0; i<data.size(); i++) {
            ret[i] = data.get(i).doubleValue();
        }
            //ret[i] = this.roundUp(data.get(i), 2);
        return ret;
    }
    
    protected int[] toIntArray(Vector<Integer> data)
    {
        if(data == null) return null;
        int[] ret = new int[data.size()];
        for(int i=0; i<data.size(); i++)
            ret[i] = data.get(i).intValue();
        return ret;
    }    

    protected String[] toStringArray(Vector<String> data)
    {
        if(data == null) return null;
        String[] ret = new String[data.size()];
        for(int i=0; i<data.size(); i++)
            ret[i] = data.get(i);
        return ret;
    }        
    
    @Override
    public String toString()
    {
        return this.id;
    }

    public InfraType getInfraType() {
        return this.infraType;
    }

    protected int calculateDistance(double e1, double e2, double n1, double n2)
    {
        return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
    }    
    
    protected double roundUp(double n, int precision )
    {
        return (double) ( Math.round(n*Math.pow(10, precision)) / Math.pow(10, precision) );
    }    

    public Properties getProperties() {
        return this.property;
    }
    
}

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class InfraList implements Serializable {

    public String name;
    private HashMap<String, InfraObject> children = new HashMap<String, InfraObject>();

    public InfraList() {
    }

    public InfraList(String n) {
            name = n;
    }

    public void addObject(InfraObject o) {
        this.children.put(o.getId(), o);
    }

    public void removeObject(InfraObject o) {
        this.children.remove(o.getId());
    }

    public <T> void getChildren(HashMap<String, T> c) {
        Iterator<String> itr = this.children.keySet().iterator();
        while(itr.hasNext())
        {
            String key = itr.next();
            c.put(key, (T)this.children.get(key));
        }
    }

    public int size() {
        return this.children.size();
    }
}

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

/**
 *
 * @author Chongmyung Park
 */
public enum InfraProperty {
    
    DownStreamNodeId, UpStreamNodeId, NextRNodeIdInSameCorridor,CorridorId,
    easting, //modify -> lon
    northing,//modify -> lat
    green, //delete, but needed
    passage,//delete, but needed
    queue,//delete, but needed
    bypass,//delete, but needed
    merge,//delete, but needed
    dets, //delete
    name, //id -> name
    forks, // new : like downstream but It is another stream name
    lon,
    lat,
    //for DMS
    description,
    width_pixels,
    height_pixels,
    downstream, //delete -> forks
    label, n_type, attach_side, transition, station_id, 
    lanes, shift, s_limit, storage, max_wait,// rnode
    
    lane, field, category, abandoned // detector
    ;    

    
}

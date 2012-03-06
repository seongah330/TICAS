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
package edu.umn.natsrl.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JCheckBox;

/**
 * Custom Checkbox Class inherited from JCheckBox for TICAS Interface
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class EvaluationCheckBox extends JCheckBox {
    
    // list including all TICASCheckBox instances on TICAS frame
    private static HashMap<String, List<EvaluationCheckBox>> boxes = new HashMap<String, List<EvaluationCheckBox>>();
    
    // which option is this checkbox for?
    private OptionType option;
    
    // construct
    public EvaluationCheckBox(String groupId, OptionType ot) {
        super();
        this.option = ot;
        ot.setCheckBox(this);
        
        List checkList = boxes.get(groupId);
        if(checkList == null) {
            checkList = new ArrayList<EvaluationCheckBox>();
            boxes.put(groupId, checkList);
        }
        // add to static list
        checkList.add(this);
    }

    // return all checkboxes list array on TICAS frame
    public static EvaluationCheckBox[] getCheckBoxes(String groupId) {
        List<EvaluationCheckBox> checkList = boxes.get(groupId);
        if(checkList == null) return null;
        return checkList.toArray(new EvaluationCheckBox[checkList.size()]);
    }

    public OptionType getOption() {
        return option;
    }
        
    
}

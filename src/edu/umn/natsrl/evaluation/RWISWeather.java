/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
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

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.weatherRWIS.datas.Atmospheric;
import edu.umn.natsrl.weatherRWIS.datas.AtmosphericType;
import edu.umn.natsrl.weatherRWIS.site.Site;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RWISWeather extends Evaluation{

        @Override
        protected void init() {
                this.name = "RWIS_WEATHER";
        }

        @Override
        protected void process() {
                if(!caching()) return;
                
                Section section = this.opts.getSection();
                for(Period period : this.opts.getPeriods()){
                        EvaluationResult res = new EvaluationResult();
                        res.setName(period.getPeriodString());
                        /** Add Time Line */
                        ArrayList times = new ArrayList();
                        times.add("");
                        times.addAll(Arrays.asList(this.opts.getPeriods()[0].getTimeline()));
                        res.addAll(res.COL_TIMELINE(), times);
                        Site site = section.getSite();
                        if(site != null){
                                if(!site.loadData(period)){
                                        JOptionPane.showMessageDialog(null, "Perod - "+period.getPeriodString()+"\n"+site.printError());
                                        break;
                                }
                                Atmospheric atm = site.getAtmospheric();
                                System.err.println("Set PCType");
                                addSiteInfo(res,atm.getPcTypetoString(),AtmosphericType.PcType.getName());
                                addSiteInfo(res,atm.getTemperature(),AtmosphericType.Temperature.getName());
                                addSiteInfo(res,atm.getPcAccum(),AtmosphericType.pcAccum.getName());
                                addSiteInfo(res,atm.getPcAccum12Hour(),AtmosphericType.pcAccum12Hour.getName());
                                addSiteInfo(res,atm.getWndSpdAvg(),AtmosphericType.WndSpdAvg.getName());
                                addSiteInfo(res,atm.getRh(),AtmosphericType.Rh.getName());
                                
                        }
                        
                        results.add(res);
                }
                
                hasResult = true;
        }

        private void addSiteInfo(EvaluationResult res, Object[] datas, String name) {
                ArrayList data = new ArrayList();
                data.add(name);
                for(Object o : datas){
                        data.add(o);
                }
                System.err.println(data.size());
                res.addColumn(data);
        }
}

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
package edu.umn.natsrl.ticas.plugin.rtchart;

import edu.umn.natsrl.ticas.plugin.rtchart.reader.StationNode;
import java.awt.Color;
import java.awt.Font;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import org.krm.tg.BasicXYDataSeries;
import org.krm.tg.Legend;
import org.krm.tg.LinePlot;
import org.krm.tg.Symbol;
import org.krm.tg.Title;
import org.krm.tg.XYDataSeries;
import org.krm.tg.live.GraphComponent;
import org.krm.tg.live.JGraphPanel;
import org.krm.tg.Graph;
/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class LiveGraph {
    

    protected Graph graph;
    protected JGraphPanel graphPanel;
    protected Hashtable<String, XYDataSeries> dataList = new Hashtable<String, XYDataSeries>();
    protected boolean isCreated = false;
    protected Legend chartLegend = new Legend();
    final Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.GRAY, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.RED, new Color(95, 159, 159), new Color(188,143, 143), new Color(255, 64, 64), new Color(92,64,51), new Color(110,139,61), new Color(238,221,130), new Color(184,134,11), new Color(205,205,0), new Color(99,86,136)};
    protected int yMax = 110;
    protected int yMin = 0;
    protected int stationCount = 0;
    protected String title;
    protected boolean lineVisibility = true;
    
    public LiveGraph() {
        BasicXYDataSeries tmpDS = new BasicXYDataSeries(this.getXSeriesType(), this.getYSeriesType());
        graph = new Graph(tmpDS);
        graphPanel = new JGraphPanel(graph);

    }

    public JGraphPanel getGraphPanel() {
        return graphPanel;
    }

    public void addData(StationNode station)
    {
        if(station == null) {
            return;
        }
        if(!isCreated) createChart(station);

        XYDataSeries ds = this.getDataSeries(station);

        // new station
        if(ds == null) {
            stationCount++;
            addStation(station);
            return;
        }

        graph.beginUpdates();
        addValue(ds, station);
        graph.endUpdates();
        graph.getVerticalAxis().setMinMaxAutoValues(false);
        graph.getVerticalAxis().setMaxValue(yMax);
        graph.getVerticalAxis().setMinValue(yMin);
    }


    protected void addStation(StationNode station)
    {
        LinePlot linePlot = createPlot(station);
        graph.addPlot(linePlot);
        addLegend(linePlot, station);
        addHint(linePlot);
    }

    public void createChart(StationNode station)
    {
        try {
            if(isCreated) return;

            Title t = new Title(graph, getTitle());
            t.setFontColor(Color.BLACK);
            t.setVertMargin(2);
            t.setBgColor(new Color(0xffffff));

            graph.setTitleHeight(40);
            graph.addTitle(t);
            graph.addLegend(new Legend(getXLegend(), Legend.BASIS_RIGHT, -5, Legend.BASIS_BOTTOM, 2));
            graph.addLegend(new Legend(getYLegend(), Legend.BASIS_LEFT, 2, Legend.BASIS_TOP, -2));

            this.isCreated = true;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void addLegend(LinePlot plot, StationNode station)
    {
        chartLegend.addObject(plot, "S"+station.getId());
        chartLegend.setVertPosition(Legend.BASIS_TOP, -25);
        chartLegend.setBgColor(new Color(0xE0,0xE0,0xF0,0xE0));
        graph.addLegend(chartLegend);
    }

    protected void addHint(LinePlot plot)
    {
        GraphComponent graphComponent = graphPanel.getComponent();
        Font hintFont = new Font("Sans Serif", Font.PLAIN, 10);
        Format hintTimeFormat = new SimpleDateFormat("HH:mm:ss");
        graphComponent.addHintPlot(plot);
        graphComponent.getHintSettings(plot).setTemplate(getHintTemplate());
        graphComponent.getHintSettings(plot).setFormat(0, hintTimeFormat);
        graphComponent.getHintSettings(plot).setFont(hintFont);

    }

    protected LinePlot createPlot(StationNode station) {
        XYDataSeries dataSeries = new BasicXYDataSeries(getXSeriesType(), getYSeriesType());
        addValue(dataSeries, station);
        LinePlot linePlot = new LinePlot(dataSeries, graph.getHorizontalAxis(), graph.getVerticalAxis());
        linePlot.setSymbol(Symbol.createSymbol(Symbol.SYMBOL_SQUARE, getColor(), 7));
        if(this.lineVisibility) linePlot.setLineColor(getColor());
        else linePlot.setLineColor(new Color(0, 0, 0, 0));
        dataList.put(station.getId(), dataSeries);
        return linePlot;
    }

    protected XYDataSeries getDataSeries(StationNode station)
    {
        return dataList.get(station.getId());
    }

    private Color getColor()
    {
        return colors[(stationCount-1)%colors.length];
    }

    public boolean isLineVisibility() {
        return lineVisibility;
    }

    public void setLineVisibility(boolean lineVisibility) {
        this.lineVisibility = lineVisibility;
    }

    public void reset()
    {
        isCreated = false;
        stationCount = 0;
        chartLegend = new Legend();
        dataList.clear();
        BasicXYDataSeries tmpDS = new BasicXYDataSeries(this.getXSeriesType(), this.getYSeriesType());
        graph = new Graph(tmpDS);
        graphPanel = new JGraphPanel(graph);
    }

    abstract protected void addValue(XYDataSeries ds, StationNode station);
    abstract protected String getTitle();
    abstract protected String getXLegend();
    abstract protected String getYLegend();
    abstract protected String getHintTemplate();
    abstract protected int getXSeriesType();
    abstract protected int getYSeriesType();

}

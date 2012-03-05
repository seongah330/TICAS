package edu.umn.natsrl.ticas.plugin.detecterdatareader;
 
import edu.umn.natsrl.ticas.plugin.ITicasPlugin;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.*;



/**
 *
 * @author Administrator
 */


public class DetecterDataReader implements ITicasPlugin{

    @Override
    public void init(PluginFrame frame) {
        Container contain = frame.getContentPane();
        contain.setLayout(new BorderLayout());
        contain.add(new DetecterDataReaderGUI(frame));
    }
    
}

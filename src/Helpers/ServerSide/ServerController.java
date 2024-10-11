package Helpers.ServerSide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class ServerController implements PropertyChangeListener {
    private ServerModel serverModel;
    private ServerView serverView;
    public ServerController() {
        this.serverModel = new ServerModel();
        this.serverView = new ServerView(this);
        serverModel.addPropertyChangeListener(this);
        serverModel.addPropertyChangeListener(serverView);

    }
    public void serverStartup() {
        try {
            serverModel.serverStartup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        // Notify the view about the change in the model
        if (evt.getPropertyName().equals("updateState")) {
            serverView.logStateUpdates((ServerModel) evt.getNewValue());
        }
    }
}

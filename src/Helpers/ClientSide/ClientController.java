package Helpers.ClientSide;

public class ClientController {
    private ClientModel clientModel;
    private ClientView clientView;
    public ClientController() {
        this.clientModel = new ClientModel();
        this.clientView = new ClientView(clientModel);
        clientModel.addObserver(clientView);
    }
    public void socketConnect() {
        try {
            clientModel.connectToServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

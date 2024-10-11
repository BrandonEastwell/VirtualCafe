package Helpers.ClientSide;

import Helpers.ClientSide.ClientModel;

public class ClientView {
    private ClientModel clientModel;
    public ClientView(ClientModel clientModel) {
        this.clientModel = clientModel;
    }
    public void outputMessage(String message) {
        System.out.println(message);
    }
    public void getUsername() {
    }
    public void setUsername(String username) {
    }
}

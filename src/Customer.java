import Helpers.ClientSide.ClientController;

//client view class
public class Customer {
    public static void main(String[] args) {
        ClientController clientController = new ClientController();
        clientController.socketConnect();
    }
}

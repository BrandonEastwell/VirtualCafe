import Helpers.ServerSide.ServerController;

//server view class
public class Barista {
    public static void main(String[] args) {
        ServerController serverController = new ServerController();
        serverController.serverStartup();
    }
}
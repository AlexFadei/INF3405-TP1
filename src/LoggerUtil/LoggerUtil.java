package LoggerUtil;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");

    public static void log(Socket clientSocket, String command) {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.printf("[%s - %s] : %s%n", clientInfo, timestamp, command);
    }
}

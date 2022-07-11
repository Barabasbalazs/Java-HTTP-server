//Barabás Balázs, 521/1, Lab2 Szerver oldal
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private String mimeType;
    private String pathOfFile;
    private final String messageOk;
    private final String messageError;
    private boolean persistent;
    private final int id;

    public ClientHandler(Socket socket,int id) {
        this.clientSocket = socket;
        mimeType = "";
        pathOfFile = "";
        messageOk = "HTTP/1.1 200 OK\r\n";
        messageError = "HTTP/1.1 404 Not Found\r\n";
        persistent = true;
        this.id = id;
    }

    @Override
    public void run() {
        while (persistent) {
            InputStream input = null;
            try {
                if (!clientSocket.isClosed())
                    input = clientSocket.getInputStream();
                else {
                    System.out.println("id : " + id + " break 1");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader reader;
            if (!clientSocket.isClosed())
                reader = new BufferedReader(new InputStreamReader(input));
            else {
                System.out.println("id : " + id + " break 2");
                break;
            }
            String line = "k";
            Path path = null;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println(line);
                if (line.startsWith("GET")) {
                    pathOfFile = getPath(line);
                    path = new File(System.getProperty("user.dir") + pathOfFile).toPath();
                    try {
                        mimeType = Files.probeContentType(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (line.equals("Connection: keep-alive")) {
                    persistent = true;
                } else if (line.equals("Connection: close")) {
                    persistent = false;
                }
                if (line.isEmpty())
                    break;
            }

            //System.out.println("mime-type is : " + mimeType);
            System.out.println("id : " + id + " Url :" + pathOfFile);

            byte[] content = null;

            boolean fileExists = true;

            if (path == null)
                path = new File("randomnkvsdv").toPath();
            try {
                content = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
                fileExists = false;
            }

            if (fileExists) {
                //System.out.println("File exists");
                send200message(content);
            } else {
                //System.out.println("File does not exist");
                try {
                    send400message();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            System.out.println("id : " + id + " closing socket");
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Thread is dying");
        //timeout to exit cycle or connection close -> this is when we shut off the connection to the socket
    }



    private String getPath(String line) {
        String path = line.substring(4);
        char c = 'k';
        int i = 0;
        while (c != ' ') {
            c = path.charAt(i);
            i++;
        }
        return path.substring(0,i - 1);
    }

    private void send200message(byte []content) {

        assert content != null;
       // System.out.println("Size of our message is: " + content.length);

        OutputStream os = null;
        try {
            os = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String t;
        byte[] s = new byte[0];
        try {
            s = messageOk.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            assert os != null;
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        t = "Content-Length: " + content.length + "\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }


        t = "Content-Type: " + mimeType + "\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (persistent)
            t = "Connection: keep-alive\r\n\r\n"; //return the same message the browser sent
        else
            t = "Connection: close\r\n\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            os.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void send400message() throws IOException {
        byte[] s = new byte[0];
        s = messageError.getBytes("UTF-8");
        OutputStream os = null;
        try {
            os = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert os != null;
        os.write(s);

        Path path = new File(System.getProperty("user.dir") + "/HTML_for_server/error.html").toPath();

        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String t;
        assert content != null;
        t = "Content-Length: " + content.length + "\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }


        t = "Content-Type: " + "text/html" + "\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        t = "Connection: close\r\n\r\n";
        try {
            s = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            os.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}

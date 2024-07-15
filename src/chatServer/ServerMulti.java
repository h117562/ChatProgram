package chatServer;

import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ServerMulti extends JFrame implements ActionListener { // 0000, 0011
   // 자동 import 단축키 ctrl+shift+o 자동 채우기 ctrl+space
   private JPanel contentPane; // GUI Member 1011
   private JTextField port_tf;

   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("서버 실행"); // Refactor-->Rename
   private JButton stop_btn = new JButton("서버 중지");

   // socket 생성 연결 부분 //2000
   private ServerSocket server_socket;
   private Socket socket;
   int port = 55555;

   // Stream 변수 //3000

   // 클라이언트 관리 //5000
   private Vector<UserInfo> user_vc = new Vector<UserInfo>();

   ServerMulti() // 생성자 //1000
   {
      init(); // GUI 초기화, 화면 생성 //1000
      start(); // 리스너 설정 메소드 //1011
      Server_start(); // 2000

   }

   private void init() // 1000
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 321, 370);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JLabel lblNewLabel_2 = new JLabel("포트 번호");
      lblNewLabel_2.setBounds(12, 245, 57, 15);
      contentPane.add(lblNewLabel_2);

      port_tf = new JTextField();
      port_tf.setBounds(81, 242, 212, 21);
      contentPane.add(port_tf);
      port_tf.setColumns(10);

      start_btn.setBounds(12, 286, 138, 23);
      contentPane.add(start_btn);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(12, 10, 281, 210);
      contentPane.add(scrollPane);

      scrollPane.setViewportView(textArea);
      textArea.setEditable(false); // 6666 화면 에디트 설정

      stop_btn.setBounds(155, 286, 138, 23);
      contentPane.add(stop_btn);

      this.setVisible(true); // 화면 보이기
   }

   private void start() // 1011
   {
      start_btn.addActionListener(this);
      stop_btn.addActionListener(this);
   }

   private void Server_start() // 2000
   {
      try {
//         port = Integer.parseInt(port_tf.getText().trim());
         server_socket = new ServerSocket(port); // 2200
      } catch (IOException e) {
         e.printStackTrace();
      }
      if (server_socket != null)
         connection(); // 1000
   }

   public void connection() { // 2000

      Thread th = new Thread(new Runnable() { // 2300
         public void run() {
            while (true) { // 4000 여러명
               try {
                  textArea.append("사용자 접속 대기중\n");
                  socket = server_socket.accept();
                  textArea.append("클아이언트 접속 완료\n");
                  UserInfo user = new UserInfo(socket);
                  user.start();

               } catch (IOException e) {
               }
            }

         }
      });
      th.start(); // 2300
   }

   @Override
   public void actionPerformed(ActionEvent e) { // 1011
      if (e.getSource() == start_btn) {
         System.out.println("Server Start button Click");
      } else if (e.getSource() == stop_btn) {
         System.out.println("Server Stop Button Click");
      }

   } // 액션 이벤트 끝

   class UserInfo extends Thread { // 4000
      // Stream 변수
      private InputStream is;
      private DataInputStream dis;
      private OutputStream os;
      private DataOutputStream dos;
      private Socket clientSocket;
      private String userID = "";

      UserInfo(Socket socket) {
         this.clientSocket = socket;
         UserNetwork(); // 4200
      }

      @Override
      public void run() { // 4100
         System.out.println("start run");
         while (true) {
            try {
               String msg = dis.readUTF();
               System.out.println("read msg = "+ msg);
               textArea.append(userID + "로부터 수신한 메시지: " + msg + "\n");

            } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }


         }

      };

      private void UserNetwork() { // 4200
         try { // 3000
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            userID = dis.readUTF();
            textArea.append(userID + " 클라이언트 접속\n");
            
            // 연결되어 있는 클라이언트들에게 가입자 정보 전달
            System.out.println("현재 접속한 클라이언트 수 = "+ user_vc.size());
            for (int i = 0; i < user_vc.size(); i++) {         //5100
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               u.send_Msg("NewUser/"+ userID);
               System.out.println("SEND NewUser");
            }
            
            //새로 접속한 클라이언트에게                        //5100
            for(int i=0; i<user_vc.size();i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               send_Msg("OldUser/"+u.userID);
            }
            
            //새로 접속한 클라이언트 등록                        //5100
            user_vc.add(this);
            
         } catch (IOException e) {
         }
      }

      public void send_Msg(String msg) {
         try {
            dos.writeUTF(msg);
         } catch (IOException e) {         }
      }
   }

   public static void main(String[] args) { // 1000
      new ServerMulti();
   }
}
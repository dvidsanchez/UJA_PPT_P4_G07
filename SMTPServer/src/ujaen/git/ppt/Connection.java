package ujaen.git.ppt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

import ujaen.git.ppt.mail.Mail;
import ujaen.git.ppt.mail.Mailbox;
import ujaen.git.ppt.smtp.RFC5321;
import ujaen.git.ppt.smtp.RFC5322;
import ujaen.git.ppt.smtp.SMTPMessage;

public class Connection implements Runnable, RFC5322 {

	public static final int S_HELO = 0;
	public static final int S_EHLO= 1;
	public static final int S_MAIL=2;
	public static final int S_RCPT=3;
	public static final int S_DATA=4;
	public static final int S_RSET=5;
	public static final int S_QUIT=6;

	protected Socket mSocket;
	protected int mEstado = S_HELO;
	private boolean mFin = false;

	public Connection(Socket s) {
		mSocket = s;
		mEstado = 0;
		mFin = false;
	}

	@Override
	public void run() {

		String inputData = null;
		String outputData = "";
		

		if (mSocket != null) {
			try {
				// Inicialización de los streams de entrada y salida
				DataOutputStream output = new DataOutputStream(
						mSocket.getOutputStream());
				BufferedReader input = new BufferedReader(
						new InputStreamReader(mSocket.getInputStream()));

				// Envío del mensaje de bienvenida
				String response = RFC5321.getReply(RFC5321.R_220) + SP + RFC5321.MSG_WELCOME
						+ RFC5322.CRLF;
				output.write(response.getBytes());
				output.flush();
				int auxi=-1;
				int aux=0;
				Mail men=new Mail();
				while (!mFin && ((inputData = input.readLine()) != null)) {
					
					System.out.println("Servidor [Recibido]> " + inputData);
				
					
					// Todo análisis del comando recibido
					SMTPMessage m = new SMTPMessage(inputData);
					if(m.getCommand()==null){
						//no se corresponde con un comando -> error de sintaxis
					}else{
						mEstado=m.getCommandId();
					}
					// TODO: Máquina de estados del protocolo
					
					if(m.getCommand()!=null){
						
						
						switch (mEstado) {
							case S_HELO:
								if(auxi<0){
									outputData = RFC5321.getReply(RFC5321.R_250)+RFC5321.getReplyMsg(RFC5321.R_250) + SP + inputData + CRLF;
									auxi=1;
								}else{
									//Mensaje de error hello donde no corresponde 
								}
								break;
							case S_EHLO:
								
									//Mensaje de error ehlo  no soportado
								
								break;
								
							case S_MAIL:
								if(auxi==1){
									outputData = RFC5321.getReply(RFC5321.R_250)+RFC5321.getReplyMsg(RFC5321.R_250)+SP+inputData+CRLF;
									auxi=2;
									men.setMailfrom(m.getArguments());
								}else{
									//Mensaje de error MAIL donde no corresponde 
								}
								break;
								
							case S_RCPT:
								if(auxi==2){
									if(Mailbox.checkRecipient(m.getArguments())){
										outputData = RFC5321.getReply(RFC5321.R_250)+RFC5321.getReplyMsg(RFC5321.R_250)+SP+inputData+CRLF;
										//auxi=3;
										if(aux==0){
											men.setRcptto(m.getArguments());
										}else{
											men.addRecipient(m.getArguments());
										}
										
										aux=1;
									}else{
										//Mensaje error ususario no encontrado
									}
									
								}else{
									//rcpt donde no corresponde
								}
								break;
								
							case S_DATA:
								if(auxi==2 && aux==1){
									outputData = RFC5321.getReply(RFC5321.R_354)+RFC5321.getReplyMsg(RFC5321.R_354)+SP+inputData+CRLF;

								}else{
									//no hay receptores añadidos
								}
								break;
								
							case S_RSET:
								inputData=null;
								outputData="";
								auxi=1;
								aux=0;
								men.delMail();
								break;
								
							case S_QUIT:
								
								break;
							
						
						}
					}
					// TODO montar la respuesta
					// El servidor responde con lo recibido
					//outputData = RFC5321.getReply(RFC5321.R_220) + SP + inputData + CRLF;
					output.write(outputData.getBytes());
					output.flush();
					if(mEstado==S_DATA){
						inputData=null;
						do{
							if(inputData!=null){
								men.addMailLine(inputData);
							}
							inputData = input.readLine();
							
						}while(inputData!=ENDMSG);
						new Mailbox(men);
						inputData=null;
						outputData="";
						auxi=1;
						aux=0;
						men.delMail();
					}
				}
				System.out.println("Servidor [Conexión finalizada]> "
						+ mSocket.getInetAddress().toString() + ":"
						+ mSocket.getPort());

				input.close();
				output.close();
				mSocket.close();
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}

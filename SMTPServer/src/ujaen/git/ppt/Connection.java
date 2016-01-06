package ujaen.git.ppt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;

import java.util.Date;


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
					//System.out.println(m.getCommand()+m.getCommandId());
					if(m.getCommand()==null){
						outputData = RFC5321.getError(RFC5321.E_500_SINTAXERROR)+RFC5321.getErrorMsg(RFC5321.E_500_SINTAXERROR) + SP + inputData + CRLF;
						//no se corresponde con un comando -> error de sintaxis
					}else{
						mEstado=m.getCommandId();
					}
					// TODO: Máquina de estados del protocolo
					
					if(m.getCommand()!=null){
						
						
						switch (mEstado) {
							case S_HELO:
								if(auxi<0){
									if(m.getArguments()!=null&&!m.getArguments().trim().isEmpty()){
										outputData = RFC5321.getReply(RFC5321.R_250)+SP+RFC5321.getReplyMsg(RFC5321.R_250) + SP + inputData + CRLF;
										men.setHost(m.getArguments());
										auxi=1;
									}else{
										outputData = RFC5321.getError(RFC5321.E_500_SINTAXERROR)+RFC5321.getErrorMsg(RFC5321.E_500_SINTAXERROR) + SP + inputData + CRLF;
										//helo sin dominio
									}
									
								}else{
									outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;
									//helo cuando no procede
								}
								break;
							case S_EHLO:
								
								outputData = RFC5321.getError(RFC5321.E_502_COMMANDNOTIMP)+SP+RFC5321.getErrorMsg(RFC5321.E_502_COMMANDNOTIMP) + SP + inputData +"EL SERVIDOR NO TIENE/SOPORTA EXTENSIONES" + CRLF;
								//comando no soportado
								
								break;
								
							case S_MAIL:
								
								if(auxi==1){
									if(m.getArguments()!=null&&!m.getArguments().trim().isEmpty()){
										outputData = RFC5321.getReply(RFC5321.R_250)+SP+RFC5321.getReplyMsg(RFC5321.R_250)+SP+inputData+CRLF;
										auxi=2;
										men.setMailfrom(m.getArguments());
									}else{
										outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;
										//mail sin origen
									}
									
								}else{
									outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;

									//Mensaje de error MAIL donde no corresponde 
								}
								break;
								
							case S_RCPT:
								if(auxi==2){
									if(m.getArguments()!=null){
										System.out.println(m.getArguments());
										if(Mailbox.checkRecipient(m.getArguments().trim())){
											outputData = RFC5321.getReply(RFC5321.R_250)+SP+RFC5321.getReplyMsg(RFC5321.R_250)+SP+inputData+CRLF;
											//auxi=3;
											if(aux==0){
												men.setRcptto(m.getArguments().trim());
											}else{
												men.addRecipient(m.getArguments().trim());
											}
											
											aux=1;
										}else{
											outputData = RFC5321.getError(RFC5321.E_551_USERNOTLOCAL)+SP+RFC5321.getErrorMsg(RFC5321.E_551_USERNOTLOCAL) + SP + inputData + CRLF;
											//Usuario no encontrado
										}
									}else{
										outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;
										//rcpt sin destinatario
									}
									
									
								}else{
									outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;

									//rcpt donde no corresponde
								}
								break;
								
							case S_DATA:
								if(auxi==2 && aux==1){
									outputData = RFC5321.getReply(RFC5321.R_354)+SP+RFC5321.getReplyMsg(RFC5321.R_354)+SP+inputData+CRLF;

								}else{
									outputData = RFC5321.getError(RFC5321.E_503_BADSEQUENCE)+SP+RFC5321.getErrorMsg(RFC5321.E_503_BADSEQUENCE) + SP + inputData + CRLF;

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
								outputData = RFC5321.getReply(RFC5321.R_221)+SP+RFC5321.getReplyMsg(RFC5321.R_221)+SP+inputData+RFC5321.MSG_BYE+CRLF;

								mFin=true;
								break;
							
						
						}
					}
					// TODO montar la respuesta
					// El servidor responde con lo recibido
					//outputData = RFC5321.getReply(RFC5321.R_220) + SP + inputData + CRLF;
					output.write(outputData.getBytes());
					output.flush();
					outputData="";
					if(mEstado==S_DATA&&aux==1){//Desde aqui se reciben lineas para el correo y se van añadiendo hasta que se introduce un punto unicamente
						
						
						inputData=null;
						do{
							if(inputData!=null){
								
								men.addMailLine(inputData);
							}
							inputData = input.readLine();
							
						}while(!inputData.equals(".")&&!inputData.equals(RFC5322.ENDMSG));
						Date fecha=new Date();
						String df="Z";
						SimpleDateFormat sdf=new SimpleDateFormat(df);
						
						
						String finalmessage="Received: from "+men.getHost()+"("+men.getHost()+"["+mSocket.getInetAddress()+"])"+";"+CRLF+"\t"+fecha+sdf.format(fecha)+CRLF;
						
						Mailbox a=new Mailbox(men,finalmessage);
						outputData="Mensaje/s enviado/s\r\n"+a.getresp();
						output.write(outputData.getBytes());
						output.flush();
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

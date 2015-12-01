package ujaen.git.ppt.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import ujaen.git.ppt.smtp.RFC5322;


public class Mail implements RFC5322{
	
	//String containing the email
	private String mMail="";
	private String mHost="";
	private String mMailfrom="";
	private String mRcptto="";
	//Size in bytes
	private int mSize=0;
	private String mIp="";

	/**
	 * Creates a new message object with the data received
	 * @param fullMail The string that contains the email.
	 */
	public Mail(String fullMail)
	{
		mMail=fullMail;
		mSize=mMail.length();
		mMailfrom="";
		mRcptto="";
		mHost="";
		mIp="";
		
	}

	public Mail() {
		mMail="";
		mMailfrom="";
		mRcptto="";
		mHost="";
		mSize=mMail.length();
		mIp="";
	}
	
	public Mail(String from,String to,String host,String ip,String mail) {
		mMail=mail;
		mSize=mMail.length();
		mMailfrom=to;
		mRcptto=to;
		mHost=host;
		mIp=ip;
	}

	public String getMail() {
		return mMail;
	}

	public void setMail(String mail) {
		this.mMail = mail;
	}

	public int getSize() {
		return mSize;
	}

	public void setSize(int size) {
		this.mSize = size;
	}
	
	public static String getTop(String message,int lines)
	{
		if(message!=null)
		{
			int endOfHeader=message.indexOf(CRLF+CRLF);
			if(endOfHeader>-1)
			{
				String header=message.substring(0,endOfHeader)+CRLF;
				String body=message.substring(endOfHeader);
				if(lines>0 && body.length()>4)
				{
					int i=0;
					
					String line="";
					header=header+CRLF;
					BufferedReader b = new BufferedReader(new StringReader(message));
					try {
						while((line=b.readLine())!=null && i<lines)
						{
							header=header+line+CRLF;
							i++;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
				return header;
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * Añade una nueva línea al correo
	 * @param line Nueva línea de correo sin CRLF
	 */
	
	public void addMailLine(String line)
	{
		mMail=mMail+line+CRLF;
	}
	
	public void addRecipient(String recipient)
	{
		this.mRcptto=this.mRcptto+";"+recipient;
	}
	

	public String getMailfrom() {
		return mMailfrom;
	}

	public void setMailfrom(String mail2) {
		this.mMailfrom = mail2;
	}

	public String getRcptto() {
		return mRcptto;
	}

	public void setRcptto(String rcptto) {
		this.mRcptto = rcptto;
	}

	public void setHost(String host) {
		this.mHost=host;
		
	}

	public String getHost() {
		
		return this.mHost;
	}

	public String getIp() {
		return mIp;
	}

	public void setIp(String ip) {
		this.mIp = ip;
	}

	
	public void addHeader(String header, String value) {
		//TODO Método que añada cabeceras al correo
		
	}
}

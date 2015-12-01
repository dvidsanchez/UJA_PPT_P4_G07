package ujaen.git.ppt.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Protocolos de Transporte Grado de Ingeniería Telemática E. P. S. de Linareas
 * Universidad de Jaén
 * 
 * Esta clase gestiona los ficheros de un directorio pasado en el constructor
 * para emular un almacén de correos electrónicos.
 * 
 * funcionamiento: Crear una carpeta con el mismo nombre del usuario en el
 * directorio de trabajo. Crear dentro de dicha carpeta cuantos ficheros de
 * texto se quiera. Cada uno de esos ficheros representará un correo electrónico
 * diferente por lo que deberán mantener el formato de la RFC 822.
 * 
 * Para almacenar la clave del usuario se debe crear en el directorio de trabajo
 * un fichero user.key, donde user es el identificador del usuario concreto. Ese
 * fichero tan solamente debe contener una línea con una cadena sin espacios que
 * será la clave.
 * 
 * @author Juan Carlos Cuevas Martínez
 * @version 2.0
 * 
 */
public class Mailbox extends ArrayList<Mail> {

	private static final long serialVersionUID = 8973138498605787460L;

	private boolean mOpenned = false;
	public static final String DEFAULT_EXTENSION = ".txt";

	/**
	 * Identificador de usuario
	 */
	protected String mUser = "";

	/**
	 * Se inicializa el buzón con el nombre del usuario
	 * 
	 * @param user
	 */
	public Mailbox(String user) {
		mUser = user;

	}

	/**
	 * Se inicializa el buzón con el nombre del usuario
	 * 
	 * @param user
	 */
	public Mailbox(Mail mail) {
		mUser = mail.getRcptto();

		this.add(mail);
		this.newMail(mail.getMail());

	}

	/**
	 * Comprueba si existe el directorio
	 * 
	 * @param user
	 * @return
	 */
	public boolean open(String key) {
		File file = new File(mUser);
		if (file.isDirectory()) {
			if (checkKey(key)) {

				mOpenned = true;
				getMessages();
			}

		} else
			mOpenned = true;

		return mOpenned;
	}

	/**
	 * Reads the whole file to generate the mailbox
	 * 
	 * @param user
	 * @return number of bytes read, -1 if an error occurred
	 */
	private int getMessages() {
		int bytes = -1;

		if (isOpenned()) {
			File file = new File(mUser);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();

				if (files != null)
					for (File f : files) {

						byte[] data = new byte[(int) f.length()];
						try {
							FileInputStream fis = new FileInputStream(f);

							bytes = fis.read(data);
							fis.close();
							add(new Mail(new String(data)));
							System.out.println(new String(data));

						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return -1;
						} catch (IOException e) {
							e.printStackTrace();
							return -1;
						}
					}
			}
		}

		return bytes;
	}

	/**
	 * Write a new file in the mailbox directory of the active user
	 * 
	 * @param mail
	 *            String that contains the email in text form
	 * @return true if the file was created, false otherwise
	 */
	public boolean newMail(String mail) {

		File file = new File(mUser);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();

			if (files != null) {
				String[] filenames = new String[files.length];
				String newfilename = String.valueOf(files.length);
				int count = files.length + 1;
				int n = 0;
				for (File f : files) {
					filenames[n] = f.getName();
					try {
						int index = Integer.parseInt(filenames[n]);
						if (index > count)
							count = index + 1;
					} catch (NumberFormatException ex) {

					} finally {
						n++;
					}

				}

				newfilename = String.valueOf(count);

				try {
					newfilename = ".\\" + mUser + "\\" + newfilename
							+ DEFAULT_EXTENSION;
					File newfile = new File(newfilename);

					FileOutputStream fos = new FileOutputStream(newfile);

					fos.write(mail.getBytes());
					fos.close();

					System.out.println("Fichero creado: " + newfilename
							+ DEFAULT_EXTENSION);
					return true;

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether an user is registered in the mail server or not
	 * 
	 * @param rcpt
	 *            String that contains the user name in text form
	 * @return true if the user exists, false otherwise
	 */
	public static boolean checkRecipient(String rcpt) {

		File file = new File(rcpt);
		if (file.exists() && file.isDirectory()) {
			return true;
		}
		return false;

	}

	/**
	 * Returns the message at index i
	 * 
	 * @param i
	 * @return
	 */
	public Mail getMail(int i) {
		return get(i);
	}

	private boolean isOpenned() {
		return mOpenned;
	}

	/**
	 * Ejemplo de implementación para el comando LIST de POP3 Este método
	 * comprueba el contenido del buzón del usuario para generar la respuesta
	 * que daría en comando LIST.
	 * 
	 * @return una cadena que contiene la respuesta del servidor al comando LIST
	 */
	public String getList() {
		int count = 1;
		int totalSize = 0;
		String list = "";

		ListIterator<Mail> e = this.listIterator();
		if (this.size() > 0) {
			while (e.hasNext()) {
				Mail m = null;

				m = e.next();
				list = list + count + " " + m.getSize() + "\r\n";
				totalSize = totalSize + m.getSize();
				count++;
			}

			list = "+OK " + (count - 1) + " messages " + totalSize
					+ " bytes\r\n" + list + ".\r\n";

		} else {
			list = "+OK 0 mensajes 0 bytes\r\n";
		}

		return list;
	}

	/**
	 * Comprueba si la clave pasada en el parámetro es correcta
	 * 
	 * @param pass
	 *            Clave a comprobar
	 * @return TRUE si las claves son correctas, FALSE en otro caso.
	 */
	public boolean checkKey(String pass) {
		File file = new File(mUser + ".key");
		if (file.exists()) {
			byte[] data = new byte[(int) file.length()];

			try {
				String key = null;
				FileInputStream fis = new FileInputStream(file);
				fis.read(data);
				fis.close();
				key = new String(data);
				return (key.compareTo(pass) == 0) ? true : false;

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return false;
	}

}

package monitoring.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.tomcat.util.codec.binary.Base64;

public class IoTConnectionUtils {
	public static String excuteMethod(String method, String targetURL,
			String urlParameters) {
		HttpsURLConnection connection = null;
		try {
			URL url = new URL(targetURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod(method);

			String encoding = new String(
					Base64.encodeBase64("a-xyzkfq-hg0yjdzk5x:Hn4s@v?2O!4@v3&(ok"
							.getBytes()));

			connection.setRequestProperty("Authorization", new StringBuilder()
					.append("Basic ").append(encoding).toString());

			connection.setRequestProperty("Content-Type", "application/json");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			print_https_cert(connection);

			return print_content(connection);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	private static void print_https_cert(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println(new StringBuilder()
						.append("Response Code : ")
						.append(con.getResponseCode()).toString());
				System.out.println(new StringBuilder()
						.append("Cipher Suite : ").append(con.getCipherSuite())
						.toString());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println(new StringBuilder()
							.append("Cert Type : ").append(cert.getType())
							.toString());
					System.out.println(new StringBuilder()
							.append("Cert Hash Code : ")
							.append(cert.hashCode()).toString());
					System.out.println(new StringBuilder()
							.append("Cert Public Key Algorithm : ")
							.append(cert.getPublicKey().getAlgorithm())
							.toString());

					System.out
							.println(new StringBuilder()
									.append("Cert Public Key Format : ")
									.append(cert.getPublicKey().getFormat())
									.toString());

					System.out.println("\n");
				}
			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String print_content(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println("****** Content of the URL ********");

				BufferedReader br = null;
				if (con.getResponseCode() >= 400) {
					br = new BufferedReader(new InputStreamReader(
							con.getErrorStream()));
				} else {
					br = new BufferedReader(new InputStreamReader(
							con.getInputStream()));
				}

				StringBuilder stringBuilder = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					stringBuilder.append(new StringBuilder().append(line)
							.append("\n").toString());
				}

				br.close();
				return stringBuilder.toString();
			} catch (IOException e) {
				con.getErrorStream();
				e.printStackTrace();
			}
		}

		return null;
	}
}
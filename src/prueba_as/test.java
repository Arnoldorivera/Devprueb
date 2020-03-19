 
package prueba_as;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;

/**
 *
 * @author Administrador
 */
public class test {
    
    
    public static void main(String []args) throws MalformedURLException, UnsupportedEncodingException, IOException, JSONException{
         //public void reportar_server() throws MalformedURLException, UnsupportedEncodingException, IOException, JSONException{
           
            
            
            URL url = new URL("http://35.225.13.244:3001/webservice/registrar_lectura");
        Map<String, Object> params = new LinkedHashMap<>();
 
        params.put("p_nci", "1417321312AB");
            params.put("p_tid", "123dsfdsdsf4156416");
            params.put("p_ubicacion", "19.15254,-103.4516");
            params.put("p_fk_id_carril", "1");
            params.put("p_fk_id_arco", "1");
            params.put("p_fk_niv", "niv1");
 
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0)
                postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()),
                    "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
 
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length",
                String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        try{
            conn.getOutputStream().write(postDataBytes);
        String cadena="";
        Reader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream(), "UTF-8"));
        for (int c = in.read(); c != -1; c = in.read())
            cadena=cadena+(char) c; //System.out.print((char) c);
            
            
            System.out.println("Respuesta contruida "+cadena);
         }catch(ConnectException ex ){
            System.out.println("Error de conexion");
            
        }
    }
}

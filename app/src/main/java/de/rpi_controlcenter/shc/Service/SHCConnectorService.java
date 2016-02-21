package de.rpi_controlcenter.shc.Service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.rpi_controlcenter.shc.Data.Room;
import de.rpi_controlcenter.shc.Data.RoomElement;

public class SHCConnectorService extends Service {

    public interface RoomListCallback {

        /**
         * wird aufgerufen wenn eine neue Räume Liste geladen wurde
         *
         * @param rooms Liste mit allen Räumen
         */
        void roomDataUpdated(List<Room> rooms);
    }

    public interface RoomElementsCallback {

        /**
         * wird aufgerufen wenn eine neue Liste mit Elementen des Raumes geladen wurde
         *
         * @param roomElements Liste mit allen Raum Elementen
         */
        void roomElementsUpdated(List<RoomElement> roomElements);
    }

    public interface CommandExecutedEvent {

        /**
         * wird aufgerufen nachdem eine Befehl gesendet wurde
         *
         * @param error Fehlertext, wenn kein fehler aufgetreten ist, leer
         */
        void commandExecuted(String error);
    }

    public interface SyncCallback {

        /**
         * wird aufgerufen wenn die Sync Daten geladen wurden
         *
         * @param roomElements Sync Daten
         */
        void syncFinished(List<RoomElement> roomElements);
    }

    /**
     * Binder deklarieren
     */
    public class SHCConnectorBinder extends Binder {

        public SHCConnectorService getSHCConnectorService() {

            return SHCConnectorService.this;
        }
    }

    // Binder Initalisieren
    private IBinder shcConnectorBinder = new SHCConnectorBinder();

    private String sessionId;

    private List<Room> roomsCache;

    private Map<Integer, List<RoomElement>> roomElementsCache = new HashMap<>();

    @Override
    public IBinder onBind(Intent intent) {

        return shcConnectorBinder;
    }

    /**
     * holt vom SHC Master Server die Daten als JSON String
     *
     * @param uri Teilstring der Anfrage nach "index.php?app=shc&"
     * @return JSON String
     */
    protected String getJsonFromShcMaster(String uri) {

        //Einstzellungsmanager holen
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //Einstellungen initslisieren
        String address = sp.getString("shc.serverIpAddress", "127.0.0.1").replace(" ", "");
        String port = sp.getString("shc.serverPort", "80").replace(" ", "");
        String location = sp.getString("shc.location", "shc").replace(" ", "");
        String user = sp.getString("shc.user", "shc").trim();
        String password = sp.getString("shc.password", "shc").trim();

        //URL vorbereiten
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(address);
        url.append(":");
        url.append(port);
        url.append("/");
        url.append(location);
        url.append("/index.php?app=shc&");

        //Benutzeranmeldung
        if(!user.equals("") && !password.equals("")) {

            try {

                url.append("user=");
                url.append(URLEncoder.encode(user, "UTF-8"));
                url.append("&password=");
                url.append(URLEncoder.encode(password, "UTF-8"));
                url.append("&");
            } catch (UnsupportedEncodingException e) {}
        }
        url.append(uri);

        try {

            //Cookie Handler initalisieren
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

            //Verbindung initalisieren
            StringBuilder response = new StringBuilder();
            URLConnection connection = new URL(url.toString()).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "SHC Android App");

            //Session Cookie setzen
            if(sessionId != null && !sessionId.equals("")) {

                connection.setRequestProperty("Cookie", "rwf_session=" + sessionId);
            }

            //Cookies lesen
            if(connection.getHeaderFields() != null) {

                List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
                if(cookies != null) {

                    for(String cookie : cookies) {

                        if(cookie.startsWith("rwf_session=")) {

                            sessionId = cookie.replace("rwf_session=", "").substring(0, 64).trim();
                            break;
                        }
                    }
                }
            }

            InputStream responseStream = connection.getInputStream();
            BufferedReader bufferedResponsse = new BufferedReader(new InputStreamReader(responseStream));

            //Eigabedaten lesen
            String line;
            while((line = bufferedResponsse.readLine()) != null){
                response.append(line);
            }

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * lädt die Liste der Räume vom SHC Server
     *
     * @param callback wird Aufgerufen nach dem laden der Daten
     */
    public void updateRoomList(final RoomListCallback callback) {

        this.updateRoomList(callback, false);
    }

    /**
     * lädt die Liste der Räume vom SHC Server
     *
     * @param callback wird Aufgerufen nach dem laden der Daten
     * @param force bei True werden immer neue Daten vom Server abgerufen
     */
    public void updateRoomList(final RoomListCallback callback, final boolean force) {

        new AsyncTask<Void, Void, List<Room>>() {

            @Override
            protected List<Room> doInBackground(Void... params) {

                //pruefen ob die Anfrage aus dem Cache bedient werden kann
                if(force == false && roomsCache != null && roomsCache.size() > 0) {

                    return roomsCache;
                }

                //Räume Liste vorbereiten
                List<Room> rooms = new ArrayList<Room>();

                //JSON String laden
                String jsonStr = getJsonFromShcMaster("a&ajax=roomsjson");

                //Fehlerüberwachung
                if(jsonStr == null) {

                    //Fehler aufgetreten
                    return null;
                } else {

                    try {

                        JSONArray jsonArray = new JSONArray(jsonStr);
                        for(int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jo = jsonArray.getJSONObject(i);
                            Room r = new Room(jo.getInt("id"), jo.getString("name"));
                            rooms.add(r);
                        }

                    } catch (JSONException e) {
                        return rooms;
                    }
                }
                roomsCache = rooms;
                return rooms;
            }

            @Override
            protected void onPostExecute(List<Room> rooms) {
                super.onPostExecute(rooms);

                callback.roomDataUpdated(rooms);
            }
        }.execute();

    }

    /**
     * lädt die Liste aller Elemente eines Raumes
     *
     * @param roomId ID des Raumes
     * @param callback
     */
    public void updateRoomElementList(final int roomId, final RoomElementsCallback callback) {

        this.updateRoomElementList(roomId, callback, false);
    }

    /**
     * lädt die Liste aller Elemente eines Raumes
     *
     * @param roomId ID des Raumes
     * @param callback
     * @param force bei True werden immer neue Daten vom Server abgerufen
     */
    public void updateRoomElementList(final int roomId, final RoomElementsCallback callback, final boolean force) {

        new AsyncTask<Void, Void, List<RoomElement>>() {

            @Override
            protected List<RoomElement> doInBackground(Void... params) {

                //pruefen ob die Anfrage aus dem Cache bedient werden kann
                if(force == false && roomElementsCache != null && roomElementsCache.containsKey(roomId) && roomElementsCache.get(roomId).size() > 0) {

                    return roomElementsCache.get(roomId);
                }

                //Räume Liste vorbereiten
                List<RoomElement> roomElements = new ArrayList<RoomElement>();

                //JSON String laden
                String jsonStr = getJsonFromShcMaster("a&ajax=roomelementsjson&id=" + roomId);

                //Fehlerüberwachung
                if(jsonStr == null) {

                    //Fehler aufgetreten
                    return null;
                } else {

                    try {

                        JSONArray jsonArray = new JSONArray(jsonStr);
                        for(int i = 0; i < jsonArray.length(); i++) {

                            //String in JSON Objekt umwandeln
                            JSONObject element = jsonArray.getJSONObject(i);

                            //Raum Element initalisieren
                            RoomElement re = new RoomElement();
                            re.setType(element.getString("type"));
                            re.setName(element.getString("name"));
                            re.setId(element.optString("id", "-1"));
                            re.setIcon(element.optString("icon", null));
                            re.setState(element.optInt("state", 0));

                            //Spezifische Daten laden
                            switch(re.getType()) {

                                case "Activity":
                                case "AvmSocket":
                                case "Countdown":
                                case "RadioSocket":
                                case "RpiGpioOutput":
                                case "EdimaxSocket":
                                case "VirtualSocket":

                                    re.addData("buttonText", element.optString("buttonText", "1"));
                                    roomElements.add(re);
                                    break;
                                case "FritzBox":

                                    re.addData("function", element.optString("function", "1"));
                                    roomElements.add(re);
                                    break;
                                case "Script":

                                    re.addData("function", element.optString("function", "both"));
                                    re.addData("buttonText", element.optString("buttonText", "1"));
                                    roomElements.add(re);
                                    break;
                                case "AvmMeasuringSocket":

                                    re.addData("temp", element.optString("temp", "-273,3 °C"));
                                    re.addData("power", element.optString("power", "-1 W"));
                                    re.addData("energy", element.optString("energy", "-1 W"));
                                    roomElements.add(re);
                                    break;
                                case "BMP":

                                    re.addData("temp", element.optString("temp", "-273,3 °C"));
                                    re.addData("press", element.optString("press", "0 hPa"));
                                    re.addData("alti", element.optString("alti", "-1000,"));
                                    roomElements.add(re);
                                    break;
                                case "DHT":

                                    re.addData("temp", element.optString("temp", "-273,3 °C"));
                                    re.addData("hum", element.optString("hum", "-10 %"));
                                    roomElements.add(re);
                                    break;
                                case "DS18x20":

                                    re.addData("temp", element.optString("temp", "-273,3 °C"));
                                    roomElements.add(re);
                                    break;
                                case "Hygrometer":

                                    re.addData("val", element.optString("val", "-10%"));
                                    roomElements.add(re);
                                    break;
                                case "LDR":

                                    re.addData("val", element.optString("val", "-10%"));
                                    roomElements.add(re);
                                    break;
                                case "RainSensor":

                                    re.addData("val", element.optString("val", "-10%"));
                                    roomElements.add(re);
                                    break;
                                case "Box":

                                    //Box Start Element
                                    RoomElement boxStart = new RoomElement();
                                    boxStart.setType("boxStart");
                                    boxStart.setName(element.getString("name"));
                                    roomElements.add(boxStart);

                                    //Box elemente
                                    JSONArray boxElements = element.getJSONArray("elements");
                                    for(int j = 0; j < boxElements.length(); j++) {

                                        JSONObject boxElement = boxElements.getJSONObject(j);

                                        //Raum Element initalisieren
                                        RoomElement bre = new RoomElement();
                                        bre.setType(boxElement.getString("type"));
                                        bre.setName(boxElement.getString("name"));
                                        bre.setId(boxElement.optString("id", "-1"));
                                        bre.setIcon(boxElement.optString("icon", null));
                                        bre.setState(boxElement.optInt("state", 0));

                                        //Spezifische Daten laden
                                        switch(bre.getType()) {

                                            case "Activity":
                                            case "AvmSocket":
                                            case "Countdown":
                                            case "RadioSocket":
                                            case "RpiGpioOutput":
                                            case "EdimaxSocket":
                                            case "VirtualSocket":

                                                bre.addData("buttonText", boxElement.optString("buttonText", "1"));
                                                roomElements.add(bre);
                                                break;
                                            case "FritzBox":

                                                bre.addData("function", boxElement.optString("function", "1"));
                                                roomElements.add(bre);
                                                break;
                                            case "Script":

                                                bre.addData("function", boxElement.optString("function", "both"));
                                                bre.addData("buttonText", boxElement.optString("buttonText", "1"));
                                                roomElements.add(bre);
                                                break;
                                            case "AvmMeasuringSocket":

                                                bre.addData("temp", boxElement.optString("temp", "-273,3 °C"));
                                                bre.addData("power", boxElement.optString("power", "-1 W"));
                                                bre.addData("energy", boxElement.optString("energy", "-1 W"));
                                                roomElements.add(bre);
                                                break;
                                            case "BMP":

                                                bre.addData("temp", boxElement.optString("temp", "-273,3 °C"));
                                                bre.addData("press", boxElement.optString("press", "0 hPa"));
                                                bre.addData("alti", boxElement.optString("alti", "-1000,"));
                                                roomElements.add(bre);
                                                break;
                                            case "DHT":

                                                bre.addData("temp", boxElement.optString("temp", "-273,3 °C"));
                                                bre.addData("hum", boxElement.optString("hum", "-10 %"));
                                                roomElements.add(bre);
                                                break;
                                            case "DS18x20":

                                                bre.addData("temp", boxElement.optString("temp", "-273,3 °C"));
                                                roomElements.add(bre);
                                                break;
                                            case "Hygrometer":

                                                bre.addData("val", boxElement.optString("val", "-10%"));
                                                roomElements.add(bre);
                                                break;
                                            case "LDR":

                                                bre.addData("val", boxElement.optString("val", "-10%"));
                                                roomElements.add(bre);
                                                break;
                                            case "RainSensor":

                                                bre.addData("val", boxElement.optString("val", "-10%"));
                                                roomElements.add(bre);
                                                break;
                                            default:

                                                roomElements.add(bre);
                                                break;
                                        }
                                    }

                                    //Box End Element
                                    RoomElement boxEnd = new RoomElement();
                                    boxEnd.setType("boxEnd");
                                    boxEnd.setName(element.getString("name"));
                                    roomElements.add(boxEnd);
                                    break;
                                default:

                                    roomElements.add(re);
                                    break;
                            }
                        }

                    } catch (JSONException e) {
                        roomElementsCache.put(roomId, roomElements);
                        return roomElements;
                    }
                }
                roomElementsCache.put(roomId, roomElements);
                return roomElements;
            }

            @Override
            protected void onPostExecute(List<RoomElement> roomElements) {
                super.onPostExecute(roomElements);

                callback.roomElementsUpdated(roomElements);
            }
        }.execute();
    }

    /**
     * ruft die Sync Daten eines Raumes ab (läuft in dem Thread in dem es gestartet wurde
     *
     * @param roomId Raum ID
     * @param callback Callback nach dem Synchronisieren
     */
    public void sync(final int roomId, final SyncCallback callback) {

        new AsyncTask<Void, Void, List<RoomElement>>() {

            @Override
            protected List<RoomElement> doInBackground(Void... params) {

                //JSON String laden
                String jsonStr = getJsonFromShcMaster("a&ajax=roomsyncjson&id=" + roomId);

                //Fehlerüberwachung
                if(jsonStr == null) {

                    //Fehler aufgetreten
                    return null;
                } else {

                    try {

                        JSONObject jsonObject = new JSONObject(jsonStr);
                        if(jsonObject.getBoolean("success")) {

                            //Erfolgreich
                            List<RoomElement> syncList = new ArrayList<>();
                            RoomElement re;
                            String key;
                            Iterator<String> iterator;

                            //Daten Einlesen
                            //Schaltbare Elemente
                            JSONObject switchables = jsonObject.optJSONObject("switchables");
                            if(switchables != null) {

                                iterator = switchables.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.setState(switchables.getInt(key));
                                    syncList.add(re);
                                }
                            }

                            //WOL
                            JSONObject wol = jsonObject.optJSONObject("wol");
                            if(wol != null) {

                                iterator = wol.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.setState(wol.getInt(key));
                                    syncList.add(re);
                                }
                            }

                            //lesbare Elemente
                            JSONObject readables = jsonObject.optJSONObject("readables");
                            if(readables != null) {

                                iterator = readables.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.setState(readables.getInt(key));
                                    syncList.add(re);
                                }
                            }

                            //DS18B20 Sensoren
                            JSONObject ds18x20 = jsonObject.optJSONObject("ds18x20");
                            if(ds18x20 != null) {

                                iterator = ds18x20.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.addData("temp", ds18x20.getJSONObject(key).getString("temp"));
                                    syncList.add(re);
                                }
                            }

                            //DHT Sensoren
                            JSONObject dht = jsonObject.optJSONObject("dht");
                            if(dht != null) {

                                iterator = dht.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.addData("temp", dht.getJSONObject(key).getString("temp"));
                                    re.addData("hum", dht.getJSONObject(key).getString("hum"));
                                    syncList.add(re);
                                }
                            }

                            //DHT Sensoren
                            JSONObject bmp = jsonObject.optJSONObject("bmp");
                            if(bmp != null) {

                                iterator = bmp.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.addData("temp", bmp.getJSONObject(key).getString("temp"));
                                    re.addData("press", bmp.getJSONObject(key).getString("press"));
                                    re.addData("alti", bmp.getJSONObject(key).getString("alti"));
                                    syncList.add(re);
                                }
                            }

                            //Analog Sensoren
                            JSONObject analog = jsonObject.optJSONObject("analog");
                            if(analog != null) {

                                iterator = analog.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.addData("val", analog.getJSONObject(key).getString("value"));
                                    syncList.add(re);
                                }
                            }

                            //DHT Sensoren
                            JSONObject avmSocket = jsonObject.optJSONObject("syncAvmPowerSocket");
                            if(avmSocket != null) {

                                iterator = avmSocket.keys();
                                while(iterator.hasNext()) {

                                    key = iterator.next();
                                    re = new RoomElement();
                                    re.setId(key);
                                    re.addData("temp", avmSocket.getJSONObject(key).getString("temp"));
                                    re.addData("power", avmSocket.getJSONObject(key).getString("power"));
                                    re.addData("energy", avmSocket.getJSONObject(key).getString("energy"));
                                    syncList.add(re);
                                }
                            }

                            return syncList;
                        } else {

                            //Fehler
                            return null;
                        }

                    } catch (JSONException e) {

                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(List<RoomElement> roomElements) {
                super.onPostExecute(roomElements);

                callback.syncFinished(roomElements);
            }
        }.execute();
    }

    public void sendOnCommand(final String elementId, final CommandExecutedEvent event) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                //JSON String laden
                String jsonStr = getJsonFromShcMaster("a&ajax=executeappswitchcommand&sid=" + elementId + "&command=1");

                if(jsonStr != null) {

                    try {

                        JSONObject jsonObject = new JSONObject(jsonStr);
                        if(jsonObject.getBoolean("success")) {

                            //Erfolgreich
                            return "";
                        } else {

                            //Fehler
                            return jsonObject.getString("message");
                        }

                    } catch (JSONException e) {

                        return e.getLocalizedMessage();
                    }
                }
                return "allgemeiner Fehler";
            }

            @Override
            protected void onPostExecute(String error) {
                super.onPostExecute(error);

                event.commandExecuted(error);
            }
        }.execute();
    }

    public void sendOffCommand(final String elementId, final CommandExecutedEvent event) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                //JSON String laden
                String jsonStr = getJsonFromShcMaster("a&ajax=executeappswitchcommand&sid=" + elementId + "&command=0");

                if(jsonStr != null) {

                    try {

                        JSONObject jsonObject = new JSONObject(jsonStr);
                        if(jsonObject.getBoolean("success")) {

                            //Erfolgreich
                            return "";
                        } else {

                            //Fehler
                            return jsonObject.getString("message");
                        }

                    } catch (JSONException e) {

                        return e.getLocalizedMessage();
                    }
                }
                return "allgemeiner Fehler";
            }

            @Override
            protected void onPostExecute(String error) {
                super.onPostExecute(error);

                event.commandExecuted(error);
            }
        }.execute();
    }
}

// IDataCallback.aidl
package edu.utsa.cs3443.servera;

// Declare any non-default types here with import statements
import edu.utsa.cs3443.servera.DataModel;

// Callback để Client nhận data
interface IDataCallback {
    void onDataReceived(DataModel data);
}
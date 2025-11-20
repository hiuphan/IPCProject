// IMyServer.aidl
package edu.utsa.cs3443.servera;

// Declare any non-default types here with import statements

import edu.utsa.cs3443.servera.IDataCallback;

interface IMyServer {
    void registerCallback(IDataCallback cb);
    void unregisterCallback(IDataCallback cb);
}
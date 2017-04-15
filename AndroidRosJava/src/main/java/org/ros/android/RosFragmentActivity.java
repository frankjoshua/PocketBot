package org.ros.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.ros.address.InetAddressFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by josh on 8/1/16.
 */
abstract public class RosFragmentActivity extends FragmentActivity {
    protected static final int MASTER_CHOOSER_REQUEST_CODE = 0;
    private final URI mCustomMasterUri;

    private ServiceConnection nodeMainExecutorServiceConnection;
    private final String notificationTicker;
    private final String notificationTitle;

    protected NodeMainExecutorService nodeMainExecutorService;

    /**
     * Listen for ROS node connection
     */
    final private ArrayList<NodeInitListener> mNodeInitListeners = new ArrayList<NodeInitListener>();


    private final class NodeMainExecutorServiceConnection implements ServiceConnection {

        private URI customMasterUri;

        public NodeMainExecutorServiceConnection(URI customUri) {
            super();
            customMasterUri = customUri;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            nodeMainExecutorService = ((NodeMainExecutorService.LocalBinder) binder).getService();

            if (customMasterUri != null) {
                nodeMainExecutorService.setMasterUri(customMasterUri);
                nodeMainExecutorService.setRosHostname(getDefaultHostAddress());
            }
            nodeMainExecutorService.addListener(new NodeMainExecutorServiceListener() {
                @Override
                public void onShutdown(NodeMainExecutorService nodeMainExecutorService) {
                    // We may have added multiple shutdown listeners and we only want to
                    // call finish() once.
                    if (!RosFragmentActivity.this.isFinishing()) {
                        RosFragmentActivity.this.finish();
                    }
                }
            });
            if (getMasterUri() == null) {
                startMasterChooser();
            } else {
                init();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    ;

    protected RosFragmentActivity(String notificationTicker, String notificationTitle) {
        this(notificationTicker, notificationTitle, null);
    }

    protected RosFragmentActivity(String notificationTicker, String notificationTitle, URI customMasterUri) {
        super();
        this.notificationTicker = notificationTicker;
        this.notificationTitle = notificationTitle;
        this.mCustomMasterUri = customMasterUri;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCustomMasterUri == null) {
            nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection(getSavedMasterUri());
        } else {
            nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection(null);
        }
    }

    /**
     * Saved ROS Master URI or null if none
     *
     * @return
     */
    abstract protected URI getSavedMasterUri();

    @Override
    protected void onStart() {
        super.onStart();
        bindNodeMainExecutorService();
    }

    protected void bindNodeMainExecutorService() {
        Intent intent = new Intent(this, NodeMainExecutorService.class);
        intent.setAction(NodeMainExecutorService.ACTION_START);
        intent.putExtra(NodeMainExecutorService.EXTRA_NOTIFICATION_TICKER, notificationTicker);
        intent.putExtra(NodeMainExecutorService.EXTRA_NOTIFICATION_TITLE, notificationTitle);
        startService(intent);
        Preconditions.checkState(
                bindService(intent, nodeMainExecutorServiceConnection, BIND_AUTO_CREATE),
                "Failed to bind NodeMainExecutorService.");
    }

    @Override
    protected void onDestroy() {
        unbindService(nodeMainExecutorServiceConnection);
        super.onDestroy();
    }

    protected void init() {
        // Run init() in a new thread as a convenience since it often requires
        // network access.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                RosFragmentActivity.this.init(nodeMainExecutorService);
                //Let everyone know that init is called
                synchronized (mNodeInitListeners) {
                    for (final NodeInitListener nodeInitListener : mNodeInitListeners) {
                        nodeInitListener.onNodeInit(nodeMainExecutorService, getMasterUri());
                    }
                    mNodeInitListeners.clear();
                }
                return null;
            }
        }.execute();
    }

    /**
     * This method is called in a background thread once this {@link Activity} has
     * been initialized with a master {@link URI} via the {@link MasterChooser}
     * and a {@link NodeMainExecutorService} has started. Your {@link NodeMain}s
     * should be started here using the provided {@link NodeMainExecutor}.
     *
     * @param nodeMainExecutor the {@link NodeMainExecutor} created for this {@link Activity}
     */
    protected abstract void init(NodeMainExecutor nodeMainExecutor);

    public void startMasterChooser() {
        Preconditions.checkState(getMasterUri() == null);
        // Call this method on super to avoid triggering our precondition in the
        // overridden startActivityForResult().
        super.startActivityForResult(new Intent(this, MasterChooser.class), 0);
    }

    public URI getMasterUri() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getMasterUri();
    }

    public String getRosHostname() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getRosHostname();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MASTER_CHOOSER_REQUEST_CODE) {
                String host = "";
                String networkInterfaceName = data.getStringExtra("ROS_MASTER_NETWORK_INTERFACE");
                // Handles the default selection and prevents possible errors
                if (networkInterfaceName == null || networkInterfaceName.equals("")) {
                    host = getDefaultHostAddress();
                } else {
                    try {
                        NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
                        host = InetAddressFactory.newNonLoopbackForNetworkInterface(networkInterface).getHostAddress();
                    } catch (SocketException e) {
                        Toast.makeText(this, "No non loopback interface found. Are you connected to wifi?", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                nodeMainExecutorService.setRosHostname(host);
                if (data.getBooleanExtra("ROS_MASTER_CREATE_NEW", false)) {
                    nodeMainExecutorService.startMaster(data.getBooleanExtra("ROS_MASTER_PRIVATE", true));
                } else {
                    URI uri;
                    try {
                        uri = new URI(data.getStringExtra("ROS_MASTER_URI"));
                    } catch (URISyntaxException e) {
                        Toast.makeText(this, "Bad ROS master URI. Format should be http://ros_master_ip:11311", Toast.LENGTH_LONG).show();
                        return;
                    }
                    nodeMainExecutorService.setMasterUri(uri);
                }
                // Run init() in a new thread as a convenience since it often requires network access.
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        RosFragmentActivity.this.init(nodeMainExecutorService);
                        return null;
                    }
                }.execute();
            } else {
                // Without a master URI configured, we are in an unusable state.
                //nodeMainExecutorService.forceShutdown();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected String getDefaultHostAddress() {
        return InetAddressFactory.newNonLoopback().getHostAddress();
    }

    public void registerNodeInitListener(final NodeInitListener nodeInitListener) {
        synchronized (mNodeInitListeners) {
            if (nodeMainExecutorService != null) {
                nodeInitListener.onNodeInit(nodeMainExecutorService, getMasterUri());
            } else {
                mNodeInitListeners.add(nodeInitListener);
            }
        }
    }

//    public void unregisterNodeInitListener(final NodeInitListener nodeInitListener){
//        synchronized (mNodeInitListeners){
//            mNodeInitListeners.add(nodeInitListener);
//        }
//    }

    public interface NodeInitListener {
        void onNodeInit(final NodeMainExecutor nodeMainExecutor, final URI masterUri);
    }
}

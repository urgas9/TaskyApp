package si.uni_lj.fri.taskyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.data.MarkerDataHolder;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;


public class FullScreenMapFragment extends Fragment implements OnMapReadyCallback {

    public static final String VIEW_HEATMAP = "heatmap";
    public static final String VIEW_MARKERS = "markers";
    private static final String TAG = "FullScreenMapFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PARAM_DEFAULT_VIEW = "default_view";
    private static final String PARAM_SHOW_LEGEND = "show_legend";
    private static final String PARAM_DATA_LIST = "data_list";
    @Bind(R.id.legend_ll)
    LinearLayout mLegendLl;
    @Bind(R.id.map_content_frame)
    FrameLayout mMapFrame;
    GoogleMap mMap;
    String[] mArrayOfComplexities;
    SupportMapFragment mMapFragment;
    HashMap<Marker, MarkerDataHolder> mMarkerHashMap = new HashMap<>();
    private String mDefaultView;
    private boolean mShowLegend;
    private ArrayList<MarkerDataHolder> mDataList;
    private ArrayList<LatLng> mLatLngArray;
    private OnFragmentInteractionListener mListener;

    public FullScreenMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param defaultView Parameter 1.
     * @return A new instance of fragment FullScreenMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FullScreenMapFragment newInstance(String defaultView, ArrayList<MarkerDataHolder> dataArray, boolean showLegend) {
        FullScreenMapFragment fragment = new FullScreenMapFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_DEFAULT_VIEW, defaultView);
        args.putBoolean(PARAM_SHOW_LEGEND, showLegend);
        args.putParcelableArrayList(PARAM_DATA_LIST, dataArray);
        fragment.setArguments(args);
        return fragment;
    }

    public static FullScreenMapFragment newInstance(String defaultView, boolean showLegend) {
        return newInstance(defaultView, null, showLegend);
    }

    public void setDataList(ArrayList<MarkerDataHolder> dataList) {
        if (dataList != null) {
            mDataList = dataList;
            if (mMap != null) {
                showDefaultView(true);
            }
        }
    }

    public void dataWasUpdated(long dbRecordId, int label) {
        Log.d(TAG, "dataWasUpdated called.");
        int index = -1, count = 0;
        for (MarkerDataHolder mdh : mDataList) {
            if (mdh.dbRecordId == dbRecordId) {
                index = count;
            }
            count++;
        }
        if (label > 0 && index >= 0) {
            mDataList.get(index).label = label;
        } else if (index >= 0) {
            mDataList.remove(index);
        }
        showDefaultView(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Log.d(TAG, "onCreate called");
        if (getArguments() != null) {
            mDefaultView = getArguments().getString(PARAM_DEFAULT_VIEW);
            if (getArguments().get(PARAM_DATA_LIST) != null) {
                mDataList = getArguments().getParcelableArrayList(PARAM_DATA_LIST);
            }
            mShowLegend = getArguments().getBoolean(PARAM_SHOW_LEGEND);
        }

        FragmentManager fm = getChildFragmentManager();
        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_content_frame);
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_content_frame, mMapFragment).commit();
        }

        mArrayOfComplexities = getResources().getStringArray(R.array.task_complexities_array);

    }

    private void showLegend() {
        if (!mShowLegend) {
            return;
        }
        mLegendLl.removeAllViews();
        for (int i = 0; i < mArrayOfComplexities.length; i++) {
            View root = LayoutInflater.from(getContext()).inflate(R.layout.legend_task_item, mLegendLl, false);
            ((TextView) root.findViewById(R.id.task_label_tv)).setText(getLabelText(i));
            root.findViewById(R.id.task_label_colorview).setBackgroundColor(AppHelper.getTaskColor(getContext(), i));
            mLegendLl.addView(root);
        }
    }

    private String getLabelText(int label) {
        if (label > 0 && label < mArrayOfComplexities.length) {
            return mArrayOfComplexities[label];
        }
        return getString(R.string.not_labeled);
    }

    public void showHeatMap(boolean updateCamera) {
        if (mMap == null || mDataList == null) {
            return;
        }
        mMap.clear();
        mDefaultView = VIEW_HEATMAP;
        mLegendLl.setVisibility(View.INVISIBLE);
        ArrayList<LatLng> latLngArray = getLatLngArray();

        TileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(latLngArray)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        if (updateCamera) {
            moveCameraToFitData(latLngArray);
        }
    }

    private ArrayList<LatLng> getLatLngArray() {

        if (mLatLngArray == null || mLatLngArray.isEmpty() || mLatLngArray.size() != mDataList.size()) {
            ArrayList<LatLng> latLngArray = new ArrayList<>();
            for (MarkerDataHolder mdh : mDataList) {
                if (mdh.latLng.latitude != 0 && mdh.latLng.longitude != 0) {
                    latLngArray.add(mdh.latLng);
                }
            }
            mLatLngArray = latLngArray;
            return latLngArray;
        } else {
            return mLatLngArray;
        }

    }

    private void moveCameraToFitData(ArrayList<LatLng> latLngArray) {
        final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngArray) {
            if (latLng.latitude != 0 && latLng.longitude != 0) {
                boundsBuilder.include(latLng);
                //mMap.addMarker(new MarkerOptions().position(latLng));
            }
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // Setting bounds to not exceed max zoom
                LatLngBounds bounds = boundsBuilder.build();
                double maxZoom = 0.002;
                LatLng sw = bounds.southwest;
                LatLng ne = bounds.northeast;
                double deltaLat = Math.abs(sw.latitude - ne.latitude);
                double deltaLon = Math.abs(sw.longitude - ne.longitude);

                if (deltaLat < maxZoom) {
                    sw = new LatLng(sw.latitude - (maxZoom - deltaLat / 2), sw.longitude);
                    ne = new LatLng(ne.latitude + (maxZoom - deltaLat / 2), ne.longitude);
                    bounds = new LatLngBounds(sw, ne);
                } else if (deltaLon < maxZoom) {
                    sw = new LatLng(sw.latitude, sw.longitude - (maxZoom - deltaLon / 2));
                    ne = new LatLng(ne.latitude, ne.longitude + (maxZoom - deltaLon / 2));
                    bounds = new LatLngBounds(sw, ne);
                }

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, AppHelper.dpToPx(getContext(), 60));
                mMap.animateCamera(cu);

                mMap.setOnCameraChangeListener(null);
            }
        });
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), mMapFrame.getMeasuredWidth(), mMapFrame.getMeasuredHeight(), 70));
    }

    private void showDefaultView(boolean updateCamera) {
        Log.d(TAG, "DEFAULT VIEW: " + mDefaultView);
        if (mDefaultView == null || mDefaultView.equals(VIEW_HEATMAP)) {
            showHeatMap(updateCamera);
        } else {
            showLabelMarkers(updateCamera);
        }
    }

    public void showLabelMarkers(boolean updateCamera) {
        if (mMap == null || mDataList == null) {
            return;
        }
        mMap.clear();

        mDefaultView = VIEW_MARKERS;
        showLegend();
        mLegendLl.setVisibility(View.VISIBLE);
        for (MarkerDataHolder mdh : mDataList) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .icon(AppHelper.getMarkerIcon(AppHelper.getTaskColor(getContext(), mdh.label)))
                    .title(getLabelText(mdh.label))
                    .snippet(mdh.time)
                    .position(mdh.latLng));
            mMarkerHashMap.put(m, mdh);
        }

        if (updateCamera) {
            moveCameraToFitData(getLatLngArray());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_full_screen_map, container, false);
        ButterKnife.bind(this, root);
        mMapFragment.getMapAsync(this);
        if (savedInstanceState != null &&
                savedInstanceState.get(PARAM_DATA_LIST) != null) {
            mDataList = savedInstanceState.getParcelableArrayList(PARAM_DATA_LIST);
            mShowLegend = savedInstanceState.getBoolean(PARAM_SHOW_LEGEND);
            mDefaultView = savedInstanceState.getString(PARAM_DEFAULT_VIEW);
        }
        return root;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PARAM_DATA_LIST, mDataList);
        outState.putBoolean(PARAM_SHOW_LEGEND, mShowLegend);
        outState.putString(PARAM_DEFAULT_VIEW, mDefaultView);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //TODO:
            /*throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                MarkerDataHolder mdh = mMarkerHashMap.get(marker);
                if (mdh != null && mdh.label <= 0) {
                    Intent labelTaskIntent = new Intent(getActivity(), LabelTaskActivity.class);
                    labelTaskIntent.putExtra("db_record_id", mdh.dbRecordId);
                    getActivity().startActivityForResult(labelTaskIntent, Constants.LABEL_TASK_REQUEST_CODE);
                } else {
                    if (mdh != null && mdh.label > 0 && mdh.label <= 5) {
                        Toast.makeText(getContext(), "This task has already been labeled!", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "Cannot get appropriate data for desired marker.");
                }
            }
        });
        Log.d(TAG, "onMapReady!");
        showDefaultView(true);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

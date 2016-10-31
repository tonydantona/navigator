package com.tonydantona.navigator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDirectionsDrawerSelectedListener} interface
 * to handle interaction events.
 */
public class DirectionsListFragment extends Fragment implements ListView.OnItemClickListener {

    private OnDirectionsDrawerSelectedListener mListener;
    // need to remove static
    public static ListView mDrawerList;

    public DirectionsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
      * @return A new instance of fragment DirectionsListFragment.
     */
    public static DirectionsListFragment newInstance() {
        DirectionsListFragment fragment = new DirectionsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MyAdapter adapter = new MyAdapter(getActivity(),MainActivity.mCurrDirections);
        mDrawerList = (ListView) getActivity().findViewById(R.id.right_drawer);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDirectionsDrawerSelectedListener) {
            mListener = (OnDirectionsDrawerSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDirectionsDrawerSelectedListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnDirectionsDrawerSelectedListener) {
            mListener = (OnDirectionsDrawerSelectedListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnDirectionsDrawerSelectedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_drawer, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MainActivity.mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            MainActivity.mDrawerLayout.closeDrawers();
        } else {
            MainActivity.mDrawerLayout.openDrawer(GravityCompat.END);
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView segment = (TextView) view.findViewById(R.id.segment);
        MainActivity.mDrawerLayout.closeDrawers();
        mListener.onSegmentSelected(segment.getText().toString());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href="http://developer.android.com/training/basics/fragments/communicating.html">Communicating with Other Fragments</a> for more information.
     */
    public interface OnDirectionsDrawerSelectedListener {
        void onSegmentSelected(String segment);
    }
}

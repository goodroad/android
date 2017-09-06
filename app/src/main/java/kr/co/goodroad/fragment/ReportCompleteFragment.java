package kr.co.goodroad.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kr.co.goodroad.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportCompleteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportCompleteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportCompleteFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "date";
    private static final String ARG_PARAM2 = "addr";
    private static final String ARG_PARAM3 = "groupType";

    private TextView mTvTime;
    private TextView mTvAddr;
    private TextView mTvGroupType;
    private Button mBtnOk;

    // TODO: Rename and change types of parameters
    private String mTime;
    private String mAddr;
    private String mGroupType;

    private OnFragmentInteractionListener mListener;

    public ReportCompleteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param date date
     * @param addr address
     * @param groupType groupType
     * @return A new instance of fragment ReportCompleteFragment.
     */
    public static ReportCompleteFragment newInstance(String date, String addr, String groupType) {
        ReportCompleteFragment fragment = new ReportCompleteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, date);
        args.putString(ARG_PARAM2, addr);
        args.putString(ARG_PARAM3, groupType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTime = getArguments().getString(ARG_PARAM1);
            mAddr = getArguments().getString(ARG_PARAM2);
            mGroupType = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_complete, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvAddr = (TextView) getView().findViewById(R.id.tv_addr);
        mTvTime = (TextView) getView().findViewById(R.id.tv_time);
        mTvGroupType = (TextView) getView().findViewById(R.id.tv_group_type);

        mTvTime.setText(mTime);
        mTvAddr.setText(mAddr);
        mTvGroupType.setText(mGroupType);

        mBtnOk = (Button) getView().findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

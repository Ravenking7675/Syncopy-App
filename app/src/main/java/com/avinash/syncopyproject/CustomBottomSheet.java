package com.avinash.syncopyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.BottomSheetAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.view.View.GONE;

public class CustomBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "CustomBottomSheet";
    private RecyclerView recyclerView;
    private BottomSheetAdapter adapter;
    private FirebaseAuth mAuth;
    private ArrayList<String> contact_list;
    private ImageView defaultI;
//    public static ShimmerFrameLayout shimmer;
    private Button doneB;
    public static ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_bottom_sheet_home);
//        shimmer = view.findViewById(R.id.bottom_sheet_Shimmer);
        defaultI = view.findViewById(R.id.bottom_sheet_defaultI);
        doneB = view.findViewById(R.id.bottom_sheet_doneB);
        progressBar = view.findViewById(R.id.bottom_sheet_progress);

        mAuth = FirebaseAuth.getInstance();

        searchConnections();

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager l = new LinearLayoutManager(getContext());
        l.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(l);

        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);


        doneB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    private void searchConnections() {
//
//        shimmer.setVisibility(View.VISIBLE);
//        shimmer.startShimmer();
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contact_list = new ArrayList<>();

                if(snapshot.exists()){

                    for(DataSnapshot s : snapshot.getChildren()){

                        String contact_id = s.getValue(String.class);
                        if(contact_id != null){

                            contact_list.add(contact_id);
                        }

                    }

                    if(contact_list.size() > 0){
                        Log.i(TAG, "onDataChange: CONTACT FOUND : "+contact_list.size());
                        defaultI.setVisibility(GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        try{
//
//                            sharedPreferences.edit().putInt(PREF_CONNECTION_COUNT, contact_list.size()).apply();
//                            updateUserInfo();
                        }catch (NullPointerException e){
                            //Do something
                            e.printStackTrace();
                        }

                        addToRecyclerView();
                    }
                    else{
                        Log.i(TAG, "onDataChange: FAILED");
                        defaultI.setVisibility(View.VISIBLE);
//                        shimmer.stopShimmer();
//                        shimmer.setVisibility(GONE);
                        progressBar.setVisibility(View.GONE);

                        recyclerView.setVisibility(GONE);

                    }

                }
                else{

                    try{
//
//                        sharedPreferences.edit().putInt(PREF_CONNECTION_COUNT, contact_list.size()).apply();
//                        updateUserInfo();
                    }catch (NullPointerException e){
                        //Do something
                        e.printStackTrace();
                    }

                    Log.i(TAG, "onDataChange: FAILED");
                    defaultI.setVisibility(View.VISIBLE);
//                    shimmer.stopShimmer();
//                    shimmer.setVisibility(GONE);
                    progressBar.setVisibility(View.GONE);

                    recyclerView.setVisibility(GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addToRecyclerView() {

        Log.i(TAG, "addToRecyclerView: START");
        adapter = new BottomSheetAdapter(getContext(), contact_list);
        recyclerView.setAdapter(adapter);

    }


}



package com.avinash.syncopyproject.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.HistoryAdapter;
import com.avinash.syncopyproject.Model.History;
import com.avinash.syncopyproject.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import anil.sardiwal.reboundrecycler.ReboundRecycler;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.view.View.GONE;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private ArrayList<History> histories;
    private FirebaseAuth mAuth;
    private ImageView emptyI;
    public static ShimmerFrameLayout shimmer;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_history);
        emptyI = view.findViewById(R.id.history_emptyI);
        shimmer = view.findViewById(R.id.history_Shimmer);

        recyclerView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();

        LinearLayoutManager l = new LinearLayoutManager(getContext());
        l.setOrientation(RecyclerView.VERTICAL);
        l.setReverseLayout(true);
        l.setStackFromEnd(true);
        recyclerView.setLayoutManager(l);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        ReboundRecycler.init(recyclerView);
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        assembleHistory();

        return view;
    }

    private void assembleHistory() {

        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();

        FirebaseDatabase.getInstance().getReference("clip").child(mAuth.getCurrentUser().getUid()).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        histories = new ArrayList<>();
                        histories.clear();

                        if(snapshot.exists()){

                            for(DataSnapshot s : snapshot.getChildren()){

                                History history = s.getValue(History.class);
                                Log.i(TAG, "TIME : "+history.getClip());
                                if(history != null){
                                    if(history.isHistory())
                                        histories.add(history);
                                }

                            }

                            if(histories.size() > 0){

                                recyclerView.setVisibility(View.VISIBLE);
                                emptyI.setVisibility(View.GONE);
                                addToRecyclerView();

                            }
                            else{
                                shimmer.stopShimmer();
                                shimmer.setVisibility(GONE);
                                recyclerView.setVisibility(View.GONE);
                                emptyI.setVisibility(View.VISIBLE);
                            }

                        }
                        else{
                            shimmer.stopShimmer();
                            shimmer.setVisibility(GONE);
                            recyclerView.setVisibility(View.GONE);
                            emptyI.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void addToRecyclerView() {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        Log.i(TAG, "addToRecyclerView: START");
        adapter = new HistoryAdapter(getContext(), histories);
        recyclerView.setAdapter(adapter);

    }
}
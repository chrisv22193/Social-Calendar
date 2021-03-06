package com.example.socialcalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FirstFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FirstFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstFragment newInstance(String param1, String param2) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private CustomCalendar customCalendar;
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference FriendsRef, CalendarPostRef;
    private ImageView addNewPostButton;

    String online_user_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_first, container, false);

        customCalendar = (CustomCalendar) v.findViewById(R.id.custom_calendar);


        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        CalendarPostRef = FirebaseDatabase.getInstance().getReference().child("CalendarPost");
//        PostRef = FirebaseDatabase.getInstance().getReference().child("Post");

//        addNewPostButton = (ImageView) v.findViewById(R.id.add_new_post_button);

        postList = (RecyclerView) v.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

//        addNewPostButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendUserToPostActivity();
//            }
//        });

        DisplayAllUsersPost();

        return v;
    }

    private void DisplayAllUsersPost() {
        Query SortByDescendingOrder = CalendarPostRef.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Events, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Events, PostViewHolder>
                        (
                                Events.class,
                                R.layout.all_calendar_post_layout,
                                PostViewHolder.class,
                                SortByDescendingOrder
                        ) {
                    @Override
                    protected void populateViewHolder(PostViewHolder postViewHolder, Events posts, int i) {
                        final String usersIDs = getRef(i).getKey();
                        CalendarPostRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    final String date = dataSnapshot.child("date").getValue().toString();
                                    final String event = dataSnapshot.child("event").getValue().toString();
                                    final String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                                    final String time = dataSnapshot.child("time").getValue().toString();
                                    final String username = dataSnapshot.child("username").getValue().toString();

                                    postViewHolder.setDate(date);
                                    postViewHolder.setEvent(event);
                                    postViewHolder.setProfileimage(profileimage);
                                    postViewHolder.setTime(time);
                                    postViewHolder.setUsername(username);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                            }
                        });
                    }
                };
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView Username = (TextView) mView.findViewById(R.id.calendar_post_user_name);
            Username.setText(username);
        }

        public void setProfileimage(String profileimage){
            ImageView image = (CircleImageView) mView.findViewById(R.id.calendar_post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setTime(String time){
            TextView PostTime = (TextView) mView.findViewById(R.id.calendar_post_time);
            PostTime.setText(time);
        }

        public void setDate(String date){
            TextView PostDate = (TextView) mView.findViewById(R.id.calendar_post_date);
            PostDate.setText(date);
        }

        public void setEvent(String event){
            TextView PostDescription = (TextView) mView.findViewById(R.id.calendar_post_description);
            PostDescription.setText(event);
        }
    }

    private void SendUserToPostActivity() {
        Intent adNewPostIntent = new Intent(getActivity(), PostActivity.class);
        startActivity(adNewPostIntent);
    }
}
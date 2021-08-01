package com.personalproject.roombuddy.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.general.ChatPage;
import com.personalproject.roombuddy.general.RoomDetails;
import com.personalproject.roombuddy.models.ChatListModel;
import com.personalproject.roombuddy.models.Messages;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.realm.Realm.getApplicationContext;

public class ChatListAdapter extends RecyclerView.Adapter{

    List<ChatListModel> chatList;
    List<String> nameList;
    Context chatListContext;




    public ChatListAdapter(Context chatListContext) {
        chatList = new ArrayList<>();
       // nameList = new ArrayList<>();
        this.chatListContext = chatListContext;
    }




    //method to add chats as soon as they are fetched
    public void addChatList(List<ChatListModel> chats) {
        int lastPos = chatList.size();
        this.chatList.addAll(chats);
        notifyItemRangeInserted(lastPos, chatList.size());
    }


//    //method to add chats as soon as they are fetched
//    public void addNameList(List<String> names) {
//        int lastPosi = nameList.size();
//        this.nameList.addAll(names);
//        notifyItemRangeInserted(lastPosi, nameList.size());
//    }



    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.chat_list_view_pager, parent, false);


        return new ChatListAdapter.MyChatListViewHolder(row);
    }





    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        final ChatListModel myChatListModel = chatList.get(position);
        //final String myNames = nameList.get(position);


        if (holder instanceof ChatListAdapter.MyChatListViewHolder) {

            ChatListAdapter.MyChatListViewHolder myChatListViewHolder = ( ChatListAdapter.MyChatListViewHolder) holder;


            //bind data with view
            Picasso.get().load(myChatListModel.getImageUrl()).networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(myChatListViewHolder.imageViewProfileThumb);

            myChatListViewHolder.name.setText(myChatListModel.getName());

            myChatListViewHolder.message.setText(myChatListModel.getMessage());



            myChatListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                          Intent  intent = new Intent(getApplicationContext(), ChatPage.class);
                            intent.putExtra("User ID", myChatListModel.getParticipantID());

                     chatListContext.startActivity(intent);

                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class MyChatListViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfileThumb;
        TextView name;
        TextView message;

        public MyChatListViewHolder(@NonNull View itemView) {
            super(itemView);


            //Hooks
            name = itemView.findViewById(R.id.chat_list_view_pager_name);
            message = itemView.findViewById(R.id.chat_list_view_pager_message);
            imageViewProfileThumb = itemView.findViewById(R.id.chat_list_view_pager_picture_thumbnail);
        }
    }
}

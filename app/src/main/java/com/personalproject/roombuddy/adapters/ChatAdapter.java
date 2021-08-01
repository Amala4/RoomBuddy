package com.personalproject.roombuddy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.models.Messages;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter{

    List<Messages> messages;
    Context chatContext;
    public static final int SENT_VIEW = 0;
    public static final int RECEIVED_VIEW = 1;
    int viewtype;





    //Constructor for the adapter
    public ChatAdapter(Context chatContext) {
        messages = new ArrayList<>();
        this.chatContext = chatContext;
    }




    //method to add messages as soon as they are fetched
    public void addMessages(List<Messages> messageList) {
        int lastPos = messages.size();
        this.messages.addAll(messageList);
        notifyItemRangeInserted(lastPos, messages.size());
    }



    //method to add each chat message as chat goes on
    public void addMessage(Messages message) {
        int lastPos = messages.size();
        this.messages.add(lastPos, message);
        notifyItemInserted(lastPos);
    }



    /*
    Decides which view to use on the chat If message
    owner is same as your ID, it means it is your
    message, hence it would use the SENT_VIEW
    which is aligned to the right of the screen
     */
    @Override
    public int getItemViewType(int position) {

        Messages myMessage = messages.get(position);
        String messageOwnerID = myMessage.getMessageOwnerID();
        String userID = myMessage.getUserID();



        if (messageOwnerID.equals(userID)){
            return SENT_VIEW;
        }
        else{
                return RECEIVED_VIEW;
        }
    }






    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        viewtype = viewType;



        //Check which view has to be used
        if (viewType == SENT_VIEW) {
            View row = inflater.inflate(R.layout.my_sent_messages, parent, false);
            return new ChatAdapter.MySentMessageViewHolder(row);
        } else  {
            View row = inflater.inflate(R.layout.my_received_messages, parent, false);
            return new ChatAdapter.MyReceivedMessageViewHolder(row);
        }

    }






    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Messages myMessage = messages.get(position);
        String messageTime = "time";                             //This is only a default declaration, the actual message time would be obtained from the instance
        Calendar rightNow = Calendar.getInstance();
        long now = rightNow.getTimeInMillis();
        long nowInSeconds = now/1000;



        //Checks if view was for sent message and binds the data accordingly
        if (holder instanceof ChatAdapter.MySentMessageViewHolder) {

            ChatAdapter.MySentMessageViewHolder mySentMessageViewHolder = ( ChatAdapter.MySentMessageViewHolder) holder;
            mySentMessageViewHolder.sentMessage.setText(myMessage.getMessage());
            long longOfMessageTime=Long.parseLong(myMessage.getMessageTime());
            long duration = nowInSeconds - longOfMessageTime;   //Finds how long ago the message was sent



            /*
            Conditions to determine if duration
            should be in minutes, hour, days etc
            */
            if (duration < 60)
            {
                messageTime = "Moments ago";
            }


            if ((59 < duration) && (duration < 3600))
            {
                int durationInMinutes = (int) (duration/60);
                if(durationInMinutes<2)
                {messageTime = durationInMinutes+" Minute ago";}
                else {messageTime = durationInMinutes+" Minutes ago";}

            }


            if ((3599 < duration) && (duration < 86400))
            {
                int durationInHours = (int) (duration/3600);

                if(durationInHours<2)
                {messageTime = durationInHours+" Hour ago";}
                else {messageTime = durationInHours+" Hours ago";}
            }


            if ((86399 < duration) && (duration < 604800))
            {
                int durationInDays = (int) (duration/86400);

                if(durationInDays<2)
                {messageTime = durationInDays+" Day ago";}
                else {messageTime = durationInDays+" Days ago";}
            }


            if ((604799 < duration) && (duration < 2419200))
            {
                int durationInWeeks = (int) (duration/604800);

                if(durationInWeeks<2)
                {messageTime = durationInWeeks+" Week ago";}
                else {messageTime = durationInWeeks+" Weeks ago";}

            }


            if ((2419199 < duration) && (duration < 29030400 ))
            {
                int durationInMonths = (int) (duration/2419200);

                if(durationInMonths<2)
                {messageTime = durationInMonths+" Month ago";}
                else {messageTime = durationInMonths+" Months ago";}

            }


            if ((29030399 < duration ))
            {
                int durationInYears = (int) (duration/29030400);

                if(durationInYears<2)
                {messageTime = durationInYears+" Year ago";}
                else {messageTime = durationInYears+" Years ago";}
            }



            mySentMessageViewHolder.sentMessageDate.setText(messageTime);
        }




        //Checks if view was for received message and binds the data accordingly
        else if (holder instanceof ChatAdapter.MyReceivedMessageViewHolder){

            ChatAdapter.MyReceivedMessageViewHolder myReceivedMessageViewHolder = ( ChatAdapter.MyReceivedMessageViewHolder) holder;
            myReceivedMessageViewHolder.receivedMessage.setText(myMessage.getMessage());
            long longOfMessageTime=Long.parseLong(myMessage.getMessageTime());
            long duration = nowInSeconds - longOfMessageTime;



               /*
            Conditions to determine if duration
            should be in minutes, hour, days etc
            */
            if (duration < 60)
            {
                messageTime = "Moments ago";
            }


            if ((59 < duration) && (duration < 3600))
            {
                int durationInMinutes = (int) (duration/60);
                if(durationInMinutes<2)
                {messageTime = durationInMinutes+" Minute ago";}
                else {messageTime = durationInMinutes+" Minutes ago";}
            }


            if ((3599 < duration) && (duration < 86400))
            {
                int durationInHours = (int) (duration/3600);

                if(durationInHours<2)
                {messageTime = durationInHours+" Hour ago";}
                else {messageTime = durationInHours+" Hours ago";}
            }


            if ((86399 < duration) && (duration < 604800))
            {
                int durationInDays = (int) (duration/86400);

                if(durationInDays<2)
                {messageTime = durationInDays+" Day ago";}
                else {messageTime = durationInDays+" Days ago";}
            }


            if ((604799 < duration) && (duration < 2419200))
            {
                int durationInWeeks = (int) (duration/604800);

                if(durationInWeeks<2)
                {messageTime = durationInWeeks+" Week ago";}
                else {messageTime = durationInWeeks+" Weeks ago";}
            }


            if ((2419199 < duration) && (duration < 29030400 ))
            {
                int durationInMonths = (int) (duration/2419200);

                if(durationInMonths<2)
                {messageTime = durationInMonths+" Month ago";}
                else {messageTime = durationInMonths+" Months ago";}
            }


            if ((29030399 < duration ))
            {
                int durationInYears = (int) (duration/29030400);

                if(durationInYears<2)
                {messageTime = durationInYears+" Year ago";}
                else {messageTime = durationInYears+" Years ago";}
            }



            myReceivedMessageViewHolder.receivedMessageDate.setText(messageTime);

        }

    }



    @Override
    public int getItemCount() {
        return messages.size();
    }



    // ViewHolder for Sent messages view
    public static class MySentMessageViewHolder extends RecyclerView.ViewHolder {

        TextView sentMessage;
        TextView sentMessageDate;



        public MySentMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            //Hooks
            sentMessage = itemView.findViewById(R.id.sent_message_body);
            sentMessageDate = itemView.findViewById(R.id.sent_message_date);
        }
    }




    // ViewHolder for Received messages view
    public static class MyReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        TextView receivedMessage;
        TextView receivedMessageDate;


        public MyReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            //Hooks
            receivedMessage = itemView.findViewById(R.id.received_message_body);
            receivedMessageDate = itemView.findViewById(R.id.received_message_date);
        }
    }


}

package com.walli_app.walli;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.sql.Date;
import java.util.Currency;
import java.util.List;

/**
 * Created by dado on 22/04/16.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView nomeGruppo;
        public TextView cash;
        public TextView lastUpdate;
        public TextView notify;
        public ImageView notify_bg;
        //info per l'intent per l'activity delle spese
        private String json;
        private String key;
        private int user_id;

        public ViewHolder(View itemView) {
            //recupero elementi dal layout
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.group_image);
            nomeGruppo= (TextView) itemView.findViewById(R.id.nomeGruppo);
            cash= (TextView) itemView.findViewById(R.id.cashi);
            lastUpdate = (TextView) itemView.findViewById(R.id.lastMod);
            notify = (TextView) itemView.findViewById(R.id.n_notify);
            notify_bg = (ImageView) itemView.findViewById(R.id.bg_notify);
            //setto onclick listener SOLO nei layout non BLANK
            if (nomeGruppo != null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(),ShowGroupShopsActivity.class);
                        intent.putExtra("group",json);
                        intent.putExtra("key",key);
                        intent.putExtra("user_id",user_id);
                        CustomAppCompatActivity act = (CustomAppCompatActivity)v.getContext();
                        act.startActivityForResult(intent, CustomAppCompatActivity.ACTIVITY_CODE);
                    }
                });
            }
        }
    }

    //const for entry type
    private final int GROUP = 0;

    private List<Group> gruppi;
    private String key;
    private int user_id;
    private long now = System.currentTimeMillis();
    private CustomAppCompatActivity me;

    public GroupAdapter(CustomAppCompatActivity me,List<Group> gruppi, String key, int user_id){
        this.gruppi = gruppi;
        this.key = key;
        this.user_id = user_id;
        this.me = me;
    }

    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //utilizzo il layout corretto
        View groupView;
        if (viewType == GROUP) groupView = inflater.inflate(R.layout.group, parent, false);
        else groupView = inflater.inflate(R.layout.blank_space, parent,false);

        // Return a new holder instance
        return new ViewHolder(groupView);
    }


    @Override
    public void onBindViewHolder(final GroupAdapter.ViewHolder viewHolder, int position) {
        if(getItemViewType(position) == GROUP){
            Group entry = gruppi.get(position);
            //salvo l'id e il nome del gruppo per le intent
            viewHolder.json = entry.toJson();
            viewHolder.key = key;
            viewHolder.user_id = user_id;
            //mostro i dati
            viewHolder.nomeGruppo.setText(entry.getName());
            Currency c = Currency.getInstance(entry.getEvaluate());
            String cash = entry.getMoney()+" "+c.getSymbol();
            viewHolder.cash.setText(cash);
            ImageManager.setImageToView(me,viewHolder.image,ImageManager.GROUP,entry.getId());
            String lastUpdate;
            //se ho una data di ultima modifica, la mostro. Altrimenti nascondo la TextView
            if(entry.getLastMod()!=null){
                Date d = entry.getLastMod();
                lastUpdate = (String) DateUtils.getRelativeTimeSpanString(d.getTime(),now,DateUtils.DAY_IN_MILLIS);
            }
            else lastUpdate = "";
            viewHolder.lastUpdate.setText(lastUpdate);
            //se ci sono notifiche le mostro, altrimenti nascondo la view e lo sfondo
            if(entry.getN_notify() > 0) {
                String s = entry.getN_notify() + "";
                viewHolder.notify.setText(s);
                viewHolder.notify.setVisibility(View.VISIBLE);
                viewHolder.notify_bg.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.notify.setVisibility(View.INVISIBLE);
                viewHolder.notify_bg.setVisibility(View.INVISIBLE);
            }
        }
    }

    //numero di item di cui sto facendo l'adapter
    @Override
    public int getItemCount() {
        return gruppi.size();
    }

    @Override
    public int getItemViewType(int position) {
        int BLANK_SPACE = 1;
        Group actual= gruppi.get(position);
        if (actual.isNull()) {
            return BLANK_SPACE;
        } else {
            return GROUP;
        }
    }


}
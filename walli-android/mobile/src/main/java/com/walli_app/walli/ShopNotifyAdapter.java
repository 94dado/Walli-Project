package com.walli_app.walli;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dado on 28/06/2016.
 */
public class ShopNotifyAdapter extends RecyclerView.Adapter<ShopNotifyAdapter.ViewHolder> {

@SuppressWarnings("deprecation")
public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

    public TextView desc;
    public TextView user_name;
    public Group group;
    public ShopNotify notify;
    private View colored;
    public int n_id;
    private ShowGroupShopsActivity activity;
    public ActionMode.Callback notifyCallback = new ActionMode.Callback() {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.shop_and_notify_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    AlertDialog.Builder d = activity.prompt(activity.getString(R.string.delete_notify));
                    d.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.deleteNotify(notify.getId());
                            mode.finish(); // chiudo il menu contestuale
                        }
                    });
                    d.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    return true;
                case R.id.mod:
                    activity.updateNotify(notify);
                    mode.finish();
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            activity.notifyActionMode = null;
            colored.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
        }
    };

    public ViewHolder(View itemView, final ShowGroupShopsActivity activity) {

        super(itemView);
        desc= (TextView) itemView.findViewById(R.id.notify_description);
        user_name = (TextView) itemView.findViewById(R.id.user_name);
        this.activity = activity;
        //setto onclick listener SOLO negli item non BLANK
        if (user_name != null){
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),CreateShopActivity.class);
                    intent.putExtra("key",activity.getDbm().toString());
                    intent.putExtra("user_id",activity.getDbm().getUserId());
                    intent.putExtra("group_id",group.getId());
                    intent.putExtra("valuta",group.getEvaluate());
                    intent.putExtra("desc",desc.getText());
                    intent.putExtra("n_id",n_id);
                    ShowGroupShopsActivity act = (ShowGroupShopsActivity) v.getContext();
                    act.startActivityForResult(intent, CustomAppCompatActivity.ACTIVITY_CODE);
                }
            });
        }
    }

    @Override
    public boolean onLongClick(View v) {
        //se un qualche menu è già mostrato, non faccio nulla
        if (activity.shopActionMode != null || activity.notifyActionMode != null || activity.debitActionMode != null) {
            return false;
        }
        ActionMode.Callback callback;
        if(activity.isAdmin() || notify.getUser_id() == activity.dbm.getUserId()){
            callback = notifyCallback;
            colored = v;
            colored.setBackgroundColor(activity.getResources().getColor(R.color.colorAccentFadeFade));
        }
        else
            callback = null;
        if(callback != null){

            activity.notifyActionMode = activity.startActionMode(callback);
            return true;
        }else{
            return false;
        }
    }
}

    private List<ShopNotify> notifiche;
    private Group group;
    private int logged_user_id;
    private String default_username;
    private ShowGroupShopsActivity activity;

    public ShopNotifyAdapter(List<ShopNotify> notifiche, ShowGroupShopsActivity activity,Group group, int user_id, String default_username){
        this.notifiche = notifiche;
        this.activity = activity;
        this.group = group;
        this.logged_user_id = user_id;
        this.default_username = default_username;
    }

    private final int NOTIFY = 0;

    @Override
    public ShopNotifyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //uso il layout corretti
        View shopNotifyView;
        if (viewType == NOTIFY) shopNotifyView = inflater.inflate(R.layout.notify_shop, parent, false);
        else shopNotifyView = inflater.inflate(R.layout.blank_space, parent,false);
        return new ViewHolder(shopNotifyView,activity);
    }



    @Override
    public void onBindViewHolder(final ShopNotifyAdapter.ViewHolder viewHolder, int position) {
        if(getItemViewType(position) == NOTIFY){
            ShopNotify entry = notifiche.get(position);
            //setto variabili per le intent
            viewHolder.group = group;
            viewHolder.notify = entry;
            viewHolder.n_id = entry.getId();
            //setto la grafica
            viewHolder.desc.setText(entry.getDescription());
            String username;
            if(entry.getUser_id() == logged_user_id){
                username = default_username;
            }else{
                username = entry.getUser_nick();
            }
            viewHolder.user_name.setText(username);
        }
    }


    @Override
    public int getItemCount() {
        return notifiche.size();
    }

    @Override
    public int getItemViewType(int position) {
        int BLANK_SPACE = 1;
        ShopNotify actual= notifiche.get(position);
        if (actual.isNull()) {
            return BLANK_SPACE;
        } else {
            return NOTIFY;
        }
    }
}
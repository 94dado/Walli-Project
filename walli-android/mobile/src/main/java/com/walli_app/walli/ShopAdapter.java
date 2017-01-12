package com.walli_app.walli;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Date;
import java.util.Currency;
import java.util.List;

/**
 * Created by dado on 22/06/2016.
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {
    @SuppressWarnings("deprecation")
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView desc;
        public TextView cash;
        public TextView lastUpdate;
        public TextView user_name;
        public TextView paied;
        public Shop shop;
        private ShowGroupShopsActivity activity;
        private View colored;
        private ActionMode.Callback shopCallback = new ActionMode.Callback() {

           //crea action mode
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.shop_and_notify_contextual_menu, menu);
                return true;
            }


            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            //click sul menu contestuale
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        AlertDialog.Builder d = activity.prompt(activity.getString(R.string.delete_shop));
                        d.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.deleteShop(shop.getId());
                                mode.finish(); // Action picked, so close the CAB
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
                        activity.isChangeableShop(shop,mode);
                        mode.finish();
                    default:
                        return false;   //non ho gestito l'evento
                }
            }

            //quando si chiude il menu contestuale
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                activity.shopActionMode = null;
                colored.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
            }
        };

        public ViewHolder(View itemView, ShowGroupShopsActivity activity) {
            super(itemView);
            desc= (TextView) itemView.findViewById(R.id.shop_description);
            cash= (TextView) itemView.findViewById(R.id.cashi);
            lastUpdate = (TextView) itemView.findViewById(R.id.lastMod);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            paied = (TextView) itemView.findViewById(R.id.paied);
            this.activity = activity;
            //se non è un item blank, setto onclick listener
            if(user_name!=null){
                itemView.setOnLongClickListener(this);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            //se un menu contestuale è già mostrato, non faccio nulla
            if (activity.shopActionMode != null || activity.notifyActionMode != null || activity.debitActionMode != null) {
                return false;
            }
            ActionMode.Callback callback;
            if(!shop.isPaid() && (activity.isAdmin() || shop.getUser_id() == activity.dbm.getUserId())){
                callback = shopCallback;
                colored = v;
                v.setBackgroundColor(activity.getResources().getColor(R.color.colorAccentFadeFade));
            }
            else
                callback = null;
            if(callback != null){
                activity.shopActionMode = activity.startActionMode(callback);
                v.setSelected(true);
                return true;
            }else{
                return false;
            }
        }
    }

    private List<Shop> spese;
    private ShowGroupShopsActivity activity;
    private Group group;
    private int logged_user_id;
    private String default_username;
    private long now = System.currentTimeMillis();

    public ShopAdapter(List<Shop> spese, ShowGroupShopsActivity activity,Group group, int user_id, String default_username){
        this.spese = spese;
        this.activity = activity;
        this.group = group;
        this.logged_user_id = user_id;
        this.default_username = default_username;
    }

    private final int SHOP = 0;

    @Override
    public ShopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        //utilizzo il corretto layout
        View shopView;
        if (viewType == SHOP) shopView = inflater.inflate(R.layout.shop, parent, false);
        else shopView = inflater.inflate(R.layout.blank_space, parent,false);
        return new ViewHolder(shopView,activity);
    }


    @Override
    public void onBindViewHolder(final ShopAdapter.ViewHolder viewHolder, int position) {
        //recupero dati
        if(getItemViewType(position) == SHOP){
           Shop entry = spese.get(position);
            //e li mostro
            viewHolder.desc.setText(entry.getDescription());
            String cash = entry.getValue();
            String currency = Currency.getInstance(group.getEvaluate()).getSymbol();
            cash = cash+" "+currency;
            viewHolder.cash.setText(cash);
            Date d = entry.getDate();
            String lastUpdate = (String) DateUtils.getRelativeTimeSpanString(d.getTime(),now,DateUtils.DAY_IN_MILLIS);
            viewHolder.lastUpdate.setText(lastUpdate);
            String username;
            viewHolder.shop = entry;
            if(viewHolder.shop.getUser_id() == logged_user_id){
                username = default_username;
            }else{
                username = entry.getUser_name();
            }
            viewHolder.user_name.setText(username);
            int visibility = entry.isPaid()?View.VISIBLE:View.INVISIBLE;
            viewHolder.paied.setVisibility(visibility);
        }
    }

    @Override
    public int getItemCount() {
        return spese.size();
    }

    @Override
    public int getItemViewType(int position) {
        int BLANK_SPACE = 1;
        Shop actual= spese.get(position);
        if (actual.isNull()) {
            return BLANK_SPACE;
        } else {
            return SHOP;
        }
    }
}

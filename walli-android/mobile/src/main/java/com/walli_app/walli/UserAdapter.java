package com.walli_app.walli;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dado on 23/06/2016.
 */
public class UserAdapter extends  RecyclerView.Adapter <UserAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name_surname;
        public TextView user_name;
        public TextView admin;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.user_image);
            name_surname = (TextView) itemView.findViewById(R.id.anagrafica_utente);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            admin = (TextView) itemView.findViewById(R.id.admin);
        }
    }

    private List<User> utenti;
    private int admin_id;
    private CustomAppCompatActivity me;

    public UserAdapter(CustomAppCompatActivity me,List<User> utenti, int admin_id){
        this.me  = me;
        this.utenti = utenti;
        this.admin_id = admin_id;
    }

    private final int USER = 0;

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView;
        if (viewType == USER) userView = inflater.inflate(R.layout.user, parent, false);
        else userView = inflater.inflate(R.layout.blank_space, parent,false);

        return new ViewHolder(userView);
    }



    @Override
    public void onBindViewHolder(final UserAdapter.ViewHolder viewHolder, int position) {

        if(getItemViewType(position) == USER){
            User entry = utenti.get(position);
            String name_surname = entry.getName()+" "+entry.getSurname();
            viewHolder.name_surname.setText(name_surname);
            viewHolder.user_name.setText(entry.getNick());
            int visibility = entry.getId() == admin_id?View.VISIBLE:View.INVISIBLE;
            viewHolder.admin.setVisibility(visibility);
            ImageManager.setImageToView(me,viewHolder.image,ImageManager.USER,entry.getId());
        }
    }

    @Override
    public int getItemCount() {
        return utenti.size();
    }

    @Override
    public int getItemViewType(int position) {
        int BLANK_SPACE = 1;
        User actual= utenti.get(position);
        if (actual.isNull()) {
            return BLANK_SPACE;
        } else {
            return USER;
        }
    }
}

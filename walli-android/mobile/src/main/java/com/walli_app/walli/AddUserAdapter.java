package com.walli_app.walli;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by dado on 01/07/2016.
 */
public class AddUserAdapter extends RecyclerView.Adapter<AddUserAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nick;
        public TextView name;
        public ImageView remover;

        public ViewHolder(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.anagrafica_utente);
            nick = (TextView) itemView.findViewById(R.id.user_name);
            remover = (ImageView) itemView.findViewById(R.id.remove_user);
        }
    }

    private ArrayList<User> utenti;
    public AddUserAdapter(ArrayList<User> utenti){
        this.utenti = utenti;
    }

    //imposto il layout dell'entry
    @Override
    public AddUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View userView = inflater.inflate(R.layout.add_user, parent, false);
        return  new ViewHolder(userView);
    }

    //setto le variabili dell'holder dell'entry
    @Override
    public void onBindViewHolder(final AddUserAdapter.ViewHolder viewHolder, int position) {
        final int pos = viewHolder.getAdapterPosition();
        User entry = utenti.get(pos);
        // Set item views based on the data model
        String name_surname = entry.getName()+" "+entry.getSurname();
        viewHolder.name.setText(name_surname);
        viewHolder.nick.setText(entry.getNick());
        //set onclick listener
        final AddUserAdapter me = this;
        viewHolder.remover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rimuovo utente dall'elenco
                utenti.remove(pos);
                me.notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return utenti.size();
    }
}

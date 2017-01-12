package com.walli_app.walli;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Currency;
import java.util.List;

/**
 * Created by dado on 28/06/2016.
 */
public class CreditDebitAdapter extends RecyclerView.Adapter <CreditDebitAdapter.ViewHolder> {

    @SuppressWarnings("deprecation")
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        public ImageView image;
        public TextView user_data;
        public TextView credit_debit;
        public TextView nick_utente;
        private View colored;
        private CreditDebit debt;
        private CustomAppCompatActivity activity;
        public ActionMode.Callback debitCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //scelgo il layout del menu da usare
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.debt_menu, menu);
                return true;
            }

            //non gestisco questo evento
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            //utente fa la scelta nel menu
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    //segna il debito come pagato
                    case R.id.pay:
                        String message = activity.getString(R.string.confirm_set_as_paid);
                        AlertDialog.Builder d = activity.prompt(message);
                        d.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.setAsPaid(debt);
                                mode.finish(); //chiudo il menu
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
                    //mando notifica per ricordare di pagare
                    case R.id.notify:
                        activity.sendPaymentNotification(debt);
                        mode.finish();
                        return true;
                    default:
                        //non gesitsco il click
                        return false;
                }
            }

            //quando distruggo il menu
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                activity.notifyActionMode = null;
                colored.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
            }
        };

        public ViewHolder(View itemView, CustomAppCompatActivity activity) {
            //recupero item del layout
            super(itemView);
            this.activity = activity;
            image = (ImageView) itemView.findViewById(R.id.user_image);
            user_data = (TextView) itemView.findViewById(R.id.anagrafica_utente);
            credit_debit = (TextView) itemView.findViewById(R.id.credit_debit);
            nick_utente = (TextView) itemView.findViewById(R.id.nick_utente);
            //setto onclick listener SOLO negli item non BLANK
            if (user_data != null){
                itemView.setOnLongClickListener(this);
            }
        }

        //chiamata per il long click sull'item del layout
        @Override
        public boolean onLongClick(View v) {
            //se nessun menu contestuale è aperto
            if (activity.shopActionMode != null || activity.notifyActionMode != null || activity.debitActionMode != null) {
                return false;
            }
            //converto la valuta in un float accettabile
            String debt_string = debt.getValue().replace(',','.');
            Double val = Double.valueOf(debt_string);
            //se sono in credito, posso aprire il menu
            if(val > 0){
                ActionMode.Callback callback = debitCallback;
                colored = v;
                colored.setBackgroundColor(activity.getResources().getColor(R.color.colorAccentFadeFade));
                activity.notifyActionMode = activity.startActionMode(callback);
                return true;
            }else return false;
        }
    }

    //campi dell'adapter
    private List<CreditDebit> crediti;
    private String valuta;
    private CustomAppCompatActivity activity;

    public CreditDebitAdapter(List<CreditDebit> crediti, String valuta,CustomAppCompatActivity activity){
        this.crediti = crediti;
        this.valuta = Currency.getInstance(valuta).getSymbol();
        this.activity = activity;
    }

    private final int CREDIT_DEBIT = 0;

    @Override
    public CreditDebitAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //scelgo quale layout utilizzare
        View userView;
        if (viewType == CREDIT_DEBIT) userView = inflater.inflate(R.layout.credit_debit, parent, false);
        else userView = inflater.inflate(R.layout.blank_space, parent,false);
        //genero il view holder
        return new ViewHolder(userView,activity);
    }


    //metodo per l'inserimento dei dati nel viewholder
    @Override
    public void onBindViewHolder(final CreditDebitAdapter.ViewHolder viewHolder, int position) {
        //metto i dati se non è un blank item
        if(getItemViewType(position) == CREDIT_DEBIT){
            //recupero elemento su cui uso adapter
            CreditDebit entry = crediti.get(position);
            User user = entry.getWith();
            //popolo il view holder
            viewHolder.debt = entry;
            String name_surname = user.getName()+" "+user.getSurname();
            viewHolder.user_data.setText(name_surname);
            String credit_debt = entry.getValue()+valuta;
            viewHolder.credit_debit.setText(credit_debt);
            viewHolder.nick_utente.setText(user.getNick());
            ImageManager.setImageToView(activity,viewHolder.image,ImageManager.USER,user.getId());
        }
    }

    //restituisce dimensione elenco dati
    @Override
    public int getItemCount() {
        return crediti.size();
    }

    //serve per indicare se l'elemento è un blank space oppure se è un item vero e proprio
    @Override
    public int getItemViewType(int position) {
        int BLANK_SPACE = 1;
        CreditDebit actual= crediti.get(position);
        if (actual.isNull()) {
            return BLANK_SPACE;
        } else {
            return CREDIT_DEBIT;
        }
    }
}

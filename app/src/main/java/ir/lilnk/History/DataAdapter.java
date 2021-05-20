package ir.lilnk.History;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import ir.lilnk.Helpers.CopyToClipboard;
import ir.lilnk.MainActivity;
import ir.lilnk.R;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

    Context context;
    ArrayList ShortURL, LongURL;

    public DataAdapter(Context context, ArrayList pass_site, ArrayList pass_Password) {
        this.context = context;
        ShortURL = pass_site;
        LongURL = pass_Password;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_item_style, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.RowShort.setText(String.valueOf(ShortURL.get(position)));
        holder.RowLong.setText(String.valueOf(LongURL.get(position)));

        if (MainActivity.QRS)
            Glide.with(context).load(QRCode.from(String.valueOf(LongURL.get(position))).withSize(512, 512).bitmap()).into(holder.RowQRCode);
        else
            Glide.with(context).load(QRCode.from(String.valueOf(ShortURL.get(position))).withSize(512, 512).bitmap()).into(holder.RowQRCode);
    }

    @Override
    public int getItemCount() {
        return ShortURL.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView RowShort, RowLong;
        ImageView RowQRCode;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            RowShort = itemView.findViewById(R.id.TV_HistoryShort);
            RowLong = itemView.findViewById(R.id.TV_HistoryLong);
            RowQRCode = itemView.findViewById(R.id.IV_HistoryQR);
            itemView.setOnClickListener(view -> {
                CopyToClipboard.setClipboard(context, RowShort.getText().toString());
                Toast.makeText(context, "لینک کوتاه کپی شد!", Toast.LENGTH_SHORT).show();
            });
            itemView.setOnLongClickListener(view -> {
                Toast.makeText(context, "بارکد ذخیره شد!", Toast.LENGTH_SHORT).show();
                if (MainActivity.QRS)
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), QRCode.from(RowLong.getText().toString()).withSize(512, 512).bitmap(), "Lilnk", RowShort.getText().toString());
                else
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), QRCode.from(RowShort.getText().toString()).withSize(512, 512).bitmap(), "Lilnk", RowShort.getText().toString());
                return true;
            });
        }
    }
}

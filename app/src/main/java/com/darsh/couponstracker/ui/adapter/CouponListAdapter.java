package com.darsh.couponstracker.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;

import java.util.ArrayList;

/**
 * Created by darshan on 13/3/17.
 */

public class CouponListAdapter extends RecyclerView.Adapter<CouponListAdapter.ViewHolder> {
    private Context context;

    private ArrayList<Coupon> coupons;
    private OnCouponClickListener onCouponClickListener;

    public CouponListAdapter(Activity activity) {
        context = activity.getApplicationContext();
        coupons = new ArrayList<>();
        onCouponClickListener = (OnCouponClickListener) activity;
    }

    public void updateCoupons(ArrayList<Coupon> tempCoupons) {
        DebugLog.logMethod();
        coupons.clear();
        coupons.addAll(tempCoupons);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item_coupon, parent, false);
        final ViewHolder viewHolder = new ViewHolder(cardView);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCouponClickListener != null) {
                    onCouponClickListener.onCouponClick(coupons.get(viewHolder.getAdapterPosition()));
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);

        holder.merchant.setText(coupon.merchant);
        holder.category.setText(coupon.category);
        holder.validUntil.setText(Utilities.getStringDate(coupon.validUntil));
        holder.couponCode.setText(coupon.couponCode);

        // Coupon has been used
        if (coupon.state == 1) {
            holder.imageViewCouponState.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_check_circle_green_24dp)
            );
            holder.textViewCouponState.setText(context.getString(R.string.coupon_state_used));
            holder.textViewCouponState.setTextColor(ContextCompat.getColor(context, R.color.material_green_700));
            return;
        }

        // Coupon is still valid
        if (coupon.validUntil >= Utilities.getLongDateToday()) {
            holder.imageViewCouponState.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_add_shopping_cart_orange_24dp)
            );
            holder.textViewCouponState.setText(context.getString(R.string.coupon_state_available));
            holder.textViewCouponState.setTextColor(ContextCompat.getColor(context, R.color.material_orange_900));
        } else {
            holder.imageViewCouponState.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_warning_red_24dp)
            );
            holder.textViewCouponState.setText(context.getString(R.string.coupon_state_unavailable));
            holder.textViewCouponState.setTextColor(ContextCompat.getColor(context, R.color.material_red_900));
        }
    }

    @Override
    public int getItemCount() {
        return coupons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        TextView merchant;
        TextView category;
        TextView validUntil;
        TextView couponCode;

        ImageView imageViewCouponState;
        TextView textViewCouponState;

        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;

            merchant = (TextView) cardView.findViewById(R.id.text_view_merchant);
            category = (TextView) cardView.findViewById(R.id.text_view_category);
            validUntil = (TextView) cardView.findViewById(R.id.text_view_valid_until);
            couponCode = (TextView) cardView.findViewById(R.id.text_view_coupon_code);

            imageViewCouponState = (ImageView) cardView.findViewById(R.id.image_view_coupon_state);
            textViewCouponState = (TextView) cardView.findViewById(R.id.text_view_coupon_state);
        }
    }

    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon);
    }

    public void releaseResources() {
        context = null;
        coupons = null;
        onCouponClickListener = null;
    }
}

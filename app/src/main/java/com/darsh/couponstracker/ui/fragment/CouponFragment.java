package com.darsh.couponstracker.ui.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.view.TextInputAutoCompleteTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by darshan on 11/3/17.
 */

public class CouponFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = "CouponFragment";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ Mode.CREATE, Mode.EDIT, Mode.VIEW })
    public @interface Mode {
        int CREATE = 10;
        int EDIT = 11;
        int VIEW = 12;
    }

    private int mode;
    private Coupon coupon;
    private ArrayList<String> merchantSuggestions;
    private ArrayList<String> categorySuggestions;

    private CompositeDisposable compositeDisposable;

    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /*
    Merchant view
     */
    @BindView(R.id.text_input_merchant)
    TextInputLayout inputLayoutMerchant;

    @BindView(R.id.auto_complete_merchant)
    TextInputAutoCompleteTextView autoCompleteMerchant;

    /*
    Category view
     */
    @BindView(R.id.text_input_category)
    TextInputLayout inputLayoutCategory;

    @BindView(R.id.auto_complete_category)
    TextInputAutoCompleteTextView autoCompleteCategory;

    /*
    Valid until view
     */
    @BindView(R.id.text_input_valid_until)
    TextInputLayout inputLayoutValidUntil;

    @BindView(R.id.edit_text_valid_until)
    TextInputEditText editTextValidUntil;

    private DatePickerDialog datePickerDialog;

    /*
    Coupon code view
     */
    @BindView(R.id.text_input_coupon_code)
    TextInputLayout inputLayoutCouponCode;

    @BindView(R.id.edit_text_coupon_code)
    TextInputEditText editTextCouponCode;

    /*
    Description view
     */
    @BindView(R.id.text_input_description)
    TextInputLayout inputLayoutDescription;

    @BindView(R.id.edit_text_description)
    TextInputEditText editTextDescription;

    @BindView(R.id.button_coupon_state)
    Button buttonCouponState;

    public CouponFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DebugLog.logMethod();

        View view = inflater.inflate(R.layout.fragment_coupon, container, false);
        unbinder = ButterKnife.bind(this, view);
        compositeDisposable = new CompositeDisposable();

        loadDataFromArguments();
        restoreSavedInstanceState(savedInstanceState);

        if (mode == 0 || coupon == null) {
            DebugLog.logMessage("mode is 0 or coupon is null");
            getActivity().onBackPressed();
        }

        initToolbar();
        setUpViews();
        showDatePickerDialog(getCalendar(savedInstanceState));
        return view;
    }

    private void loadDataFromArguments() {
        DebugLog.logMethod();
        DebugLog.logMessage("getArguments == null ? " + (getArguments() == null));
        if (getArguments() == null) {
            return;
        }

        mode = getArguments().getInt(Constants.COUPON_FRAGMENT_MODE);
        coupon = getArguments().getParcelable(Constants.COUPON_PARCELABLE);
        merchantSuggestions = getArguments().getStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS);
        categorySuggestions = getArguments().getStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS);
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        DebugLog.logMethod();
        DebugLog.logMessage("savedInstanceState == null ? " + (savedInstanceState == null));
        if (savedInstanceState == null) {
            return;
        }

        mode = savedInstanceState.getInt(Constants.COUPON_FRAGMENT_MODE);
        coupon = savedInstanceState.getParcelable(Constants.COUPON_PARCELABLE);
        merchantSuggestions = savedInstanceState.getStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS);
        categorySuggestions = savedInstanceState.getStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS);
    }

    private void initToolbar() {
        DebugLog.logMethod();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        toolbar.inflateMenu(R.menu.update_actions);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_save: {
                        DebugLog.logMessage("MenuItem Save");
                        toggleErrorView(false);
                        validateAndSave();
                        break;
                    }

                    case R.id.menu_item_edit: {
                        DebugLog.logMessage("MenuItem Edit");
                        mode = Mode.EDIT;
                        updateToolbar();
                        setViewState();
                        setUpButtonCouponState();
                        break;
                    }

                    case R.id.menu_item_delete: {
                        DebugLog.logMessage("MenuItem Delete");
                        deleteCoupon();
                        break;
                    }

                    case R.id.menu_item_share: {
                        DebugLog.logMessage("MenuItem Share");
                        ShareCompat.IntentBuilder
                                .from(getActivity())
                                .setType("text/plain")
                                .setText(getCouponShareData())
                                .setChooserTitle(getString(R.string.share_coupon_via))
                                .startChooser();
                        break;
                    }

                    default: {
                        DebugLog.logMessage("MenuItem Default");
                        break;
                    }
                }
                return true;
            }
        });
        updateToolbar();
    }

    private void toggleErrorView(boolean isErrorEnabled) {
        DebugLog.logMethod();
        if (!isErrorEnabled) {
            inputLayoutMerchant.setError(null);
            inputLayoutCategory.setError(null);
            inputLayoutValidUntil.setError(null);
            inputLayoutCouponCode.setError(null);
            inputLayoutDescription.setError(null);
        }
        inputLayoutMerchant.setErrorEnabled(isErrorEnabled);
        inputLayoutCategory.setErrorEnabled(isErrorEnabled);
        inputLayoutValidUntil.setErrorEnabled(isErrorEnabled);
        inputLayoutCouponCode.setErrorEnabled(isErrorEnabled);
        inputLayoutDescription.setErrorEnabled(isErrorEnabled);
    }

    private void updateToolbar() {
        DebugLog.logMethod();
        updateToolbarTitle();
        updateToolbarMenu();
    }

    private void updateToolbarTitle() {
        DebugLog.logMethod();
        String title = "";
        if (mode == Mode.CREATE) {
            title = getString(R.string.title_create_coupon);
        } else if (mode == Mode.EDIT) {
            title = getString(R.string.title_edit_coupon);
        } else if (mode == Mode.VIEW) {
            title = getString(R.string.title_view_coupon);
        }
        toolbar.setTitle(title);
    }

    private void updateToolbarMenu() {
        DebugLog.logMethod();
        Menu menu = toolbar.getMenu();
        if (menu == null) {
            DebugLog.logMessage("Menu null");
            return;
        }

        boolean flag = true;
        if (mode == Mode.CREATE
                || mode == Mode.EDIT
                || coupon.state == 1
                || Utilities.isCouponExpired(coupon.validUntil)) {
            flag = false;
        }
        if (menu.findItem(R.id.menu_item_edit) != null) {
            menu.findItem(R.id.menu_item_edit).setVisible(flag);
        }

        flag = mode == Mode.VIEW;
        if (menu.findItem(R.id.menu_item_delete) != null) {
            menu.findItem(R.id.menu_item_delete).setVisible(flag);
        }
        if (menu.findItem(R.id.menu_item_share) != null) {
            menu.findItem(R.id.menu_item_share).setVisible(flag);
        }
        if (menu.findItem(R.id.menu_item_save) != null) {
            menu.findItem(R.id.menu_item_save).setVisible(!flag);
        }
    }

    private void setUpViews() {
        DebugLog.logMethod();
        setUpSuggestions();

        if (!coupon.merchant.equals(Coupon.DUMMY_STRING)) {
            autoCompleteMerchant.setText(coupon.merchant);
        }
        if (!coupon.category.equals(Coupon.DUMMY_STRING)) {
            autoCompleteCategory.setText(coupon.category);
        }

        if (coupon.validUntil != Coupon.DUMMY_LONG) {
            editTextValidUntil.setText(Utilities.getStringDate(coupon.validUntil));
        }
        editTextValidUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(Calendar.getInstance());
            }
        });

        if (!coupon.couponCode.equals(Coupon.DUMMY_STRING)) {
            editTextCouponCode.setText(coupon.couponCode);
        }
        if (!coupon.description.equals(Coupon.DUMMY_STRING)) {
            editTextDescription.setText(coupon.description);
        }

        setViewState();
        setUpButtonCouponState();
    }

    private void setUpSuggestions() {
        DebugLog.logMethod();
        ArrayAdapter<String> merchantSuggestionsAdapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
        merchantSuggestionsAdapter.addAll(merchantSuggestions);
        autoCompleteMerchant.setAdapter(merchantSuggestionsAdapter);

        ArrayAdapter<String> categorySuggestionsAdapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
        categorySuggestionsAdapter.addAll(categorySuggestions);
        autoCompleteCategory.setAdapter(categorySuggestionsAdapter);
    }

    private void setViewState() {
        DebugLog.logMethod();
        boolean isEnabled = mode != Mode.VIEW;
        DebugLog.logMessage("isEnabled: " + isEnabled);
        inputLayoutMerchant.setEnabled(isEnabled);
        autoCompleteMerchant.setEnabled(isEnabled);
        autoCompleteMerchant.setFocusable(isEnabled);
        autoCompleteMerchant.setFocusableInTouchMode(isEnabled);

        inputLayoutCategory.setEnabled(isEnabled);
        autoCompleteCategory.setEnabled(isEnabled);
        autoCompleteCategory.setFocusable(isEnabled);
        autoCompleteCategory.setFocusableInTouchMode(isEnabled);

        inputLayoutValidUntil.setEnabled(isEnabled);
        editTextValidUntil.setEnabled(isEnabled);

        inputLayoutCouponCode.setEnabled(isEnabled);
        editTextCouponCode.setEnabled(isEnabled);

        inputLayoutDescription.setEnabled(isEnabled);
        editTextDescription.setEnabled(isEnabled);
    }

    private void showDatePickerDialog(Calendar calendar) {
        DebugLog.logMethod();
        if (calendar == null) {
            DebugLog.logMessage("Null calendar");
            return;
        }

        DebugLog.logMessage("Calendar time: " + calendar.getTime().toString());
        datePickerDialog = new DatePickerDialog(
                getContext(),
                R.style.PickerTheme,
                CouponFragment.this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.setTitle(getString(R.string.title_date_picker));
        datePickerDialog.show();
    }

    private Calendar getCalendar(Bundle savedInstanceState) {
        DebugLog.logMethod();
        if (savedInstanceState == null
                || !savedInstanceState.getBoolean(Constants.DATE_PICKER_SHOWING)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, savedInstanceState.getInt(Constants.DATE_PICKER_YEAR));
        calendar.set(Calendar.MONTH, savedInstanceState.getInt(Constants.DATE_PICKER_MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(Constants.DATE_PICKER_DAY_OF_MONTH));
        return calendar;
    }

    private void validateAndSave() {
        DebugLog.logMethod();
        getDataFromViews();

        boolean hasError = false;
        if (coupon.merchant.equals(Coupon.DUMMY_STRING)
                || coupon.merchant.length() < 2
                || coupon.merchant.length() > 20) {
            inputLayoutMerchant.setError(getString(R.string.error_invalid_merchant));
            hasError = true;
        }
        if (coupon.category.length() < 2
                || coupon.category.length() > 20) {
            inputLayoutCategory.setError(getString(R.string.error_invalid_category));
            hasError = true;
        }
        if (coupon.validUntil == Coupon.DUMMY_LONG) {
            inputLayoutValidUntil.setError(getString(R.string.error_invalid_date));
            hasError = true;
        }
        if (coupon.description.equals(Coupon.DUMMY_STRING)
                || coupon.description.length() < 2
                || coupon.description.length() > 150) {
            inputLayoutDescription.setError(getString(R.string.error_invalid_description));
            hasError = true;
        }
        if (hasError) {
            toggleErrorView(true);
            return;
        }

        if (mode == Mode.CREATE) {
            saveCoupon();
        } else if (mode == Mode.EDIT) {
            updateCoupon();
        }
    }

    private void getDataFromViews() {
        DebugLog.logMethod();
        if (autoCompleteMerchant.getText() != null) {
            coupon.merchant = autoCompleteMerchant.getText().toString();
        }
        if (autoCompleteCategory.getText() != null) {
            coupon.category = autoCompleteCategory.getText().toString();
        }
        if (editTextCouponCode.getText() != null) {
            coupon.couponCode = editTextCouponCode.getText().toString();
        }
        /*
        On clicking the editTextCouponCode, the editText is set to an empty string.
        Hence if coupon code is null or empty, set it to "No code required".
         */
        if (TextUtils.isEmpty(coupon.couponCode)) {
            coupon.couponCode = getString(R.string.no_code_required);
        }
        if (editTextDescription.getText() != null) {
            coupon.description = editTextDescription.getText().toString();
        }
    }

    private void saveCoupon() {
        DebugLog.logMethod();
        Single<Boolean> saveCouponSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("saveCouponSingle - call");
                return insertCouponInDb();
            }
        });
        saveCouponSingle.observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = saveCouponSingle
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        DebugLog.logMessage("saveCouponSingle - onSuccess");
                        Utilities.showToast(getContext(), getString(R.string.coupon_add_successful));
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        DebugLog.logMessage("saveCouponSingle - onError");
                        DebugLog.logMessage(e.getMessage());
                        Utilities.showToast(getContext(), getString(R.string.coupon_add_unsuccessful));
                    }
                });
        compositeDisposable.add(disposableSingleObserver);
    }

    private boolean insertCouponInDb() {
        DebugLog.logMethod();
        Uri uri = getContext().getContentResolver().insert(
                CouponContract.CouponTable.URI,
                Coupon.getContentValues(coupon)
        );
        if (uri == null || uri.getLastPathSegment().equals("-1")) {
            throw new SQLException("Error inserting coupon");
        }
        return true;
    }

    private void updateCoupon() {
        DebugLog.logMethod();
        Single<Boolean> updateCouponSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("updateCouponSingle - call");
                return updateCouponInDb();
            }
        });
        updateCouponSingle.observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = updateCouponSingle
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        DebugLog.logMessage("updateCouponSingle - onSuccess");
                        Utilities.showToast(getContext(), getString(R.string.coupon_update_successful));
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        DebugLog.logMessage("updateCouponSingle - onError");
                        DebugLog.logMessage(e.getMessage());
                        Utilities.showToast(getContext(), getString(R.string.coupon_update_unsuccessful));
                    }
                });
        compositeDisposable.add(disposableSingleObserver);
    }

    private boolean updateCouponInDb() {
        DebugLog.logMethod();
        int count = getContext().getContentResolver().update(
                CouponContract.CouponTable.makeUriForCoupon(coupon.id),
                Coupon.getContentValues(coupon),
                null,
                null
        );
        if (count != 1) {
            throw new SQLException("Coupon update failed");
        }
        return true;
    }

    private void setUpButtonCouponState() {
        DebugLog.logMethod();
        /*
        If it is in create or edit coupon mode, do not show
        the button indication coupon state.
         */
        if (mode == Mode.CREATE || mode == Mode.EDIT) {
            DebugLog.logMessage("CREATE | EDIT");
            buttonCouponState.setVisibility(View.GONE);
            return;
        }

        int couponStateColor = R.color.material_green_700;
        String couponStateText = getString(R.string.button_coupon_state_available);
        if (coupon.state == 1) {
            // Coupon has already been used
            couponStateText = getString(R.string.button_coupon_state_used);
            couponStateColor = R.color.material_grey_700;
        } else if (Utilities.isCouponExpired(coupon.validUntil)) {
            // Coupon has expired
            couponStateText = getString(R.string.button_coupon_state_unavailable);
            couponStateColor = R.color.material_grey_700;
        }
        buttonCouponState.setText(couponStateText);
        buttonCouponState.setBackgroundColor(ContextCompat.getColor(getContext(), couponStateColor));

        // Set listener for button click events
        if (!couponStateText.equals(getString(R.string.button_coupon_state_available))) {
            // If coupon has already been used or coupon has expired, disable button
            buttonCouponState.setEnabled(false);
            buttonCouponState.setOnClickListener(null);

        } else {
            buttonCouponState.setEnabled(true);
            buttonCouponState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DebugLog.logMessage("buttonCouponState - onClick");
                    useCoupon();
                }
            });
        }
        buttonCouponState.setVisibility(View.VISIBLE);
    }

    private void deleteCoupon() {
        DebugLog.logMethod();
        Single<Boolean> deleteCouponSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("deleteCouponSingle - call");
                return deleteCouponInDb();
            }
        });
        deleteCouponSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = deleteCouponSingle.subscribeWith(new DisposableSingleObserver<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                DebugLog.logMessage("deleteCouponSingle - onSuccess");
                Utilities.showToast(getContext(), getString(R.string.coupon_delete_successful));
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }

            @Override
            public void onError(Throwable e) {
                DebugLog.logMessage("deleteCouponSingle - onError");
                Utilities.showToast(getContext(), getString(R.string.coupon_delete_unsuccessful));
            }
        });
        compositeDisposable.add(disposableSingleObserver);
    }

    private boolean deleteCouponInDb() {
        DebugLog.logMethod();
        int count = getContext().getContentResolver().delete(
                CouponContract.CouponTable.makeUriForCoupon(coupon.id),
                null,
                null
        );
        /*
        If number of deleted rows is not 1, then it implies that the coupon
        does not exist. However that scenario can not occur since only the
        coupons present in db are loaded. Hence throw an SQLException if
        count != -1, so that delete failed toast can be shown.
         */
        if (count != 1) {
            throw new SQLException("Failed to delete row");
        }
        return true;
    }

    private String getCouponShareData() {
        DebugLog.logMethod();
        DebugLog.logMessage("Coupon offered by " + coupon.merchant
                + "\nValid until: " + Utilities.getStringDate(coupon.validUntil)
                + "\nCoupon code: " + coupon.couponCode
                + "\nDescription: " + coupon.description);

        return "Coupon offered by " + coupon.merchant
                + "\nValid until: " + Utilities.getStringDate(coupon.validUntil)
                + "\nCoupon code: " + coupon.couponCode
                + "\nDescription: " + coupon.description;
    }

    private void useCoupon() {
        DebugLog.logMethod();
        Single<Boolean> useCouponSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("useCouponSingle - call");
                return updateCouponState();
            }
        });
        useCouponSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = useCouponSingle.subscribeWith(new DisposableSingleObserver<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                DebugLog.logMessage("useCouponSingle - onSuccess");
                Utilities.showToast(getContext(), getString(R.string.coupon_state_update_successful));
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                DebugLog.logMessage("useCouponSingle - onError");
                DebugLog.logMessage(e.getMessage());
                Utilities.showToast(getContext(), getString(R.string.coupon_state_update_unsuccessful));
            }
        });
        compositeDisposable.add(disposableSingleObserver);
    }

    private boolean updateCouponState() {
        DebugLog.logMethod();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CouponContract.CouponTable.COLUMN_COUPON_STATE, 1);
        int count = getContext().getContentResolver().update(
                CouponContract.CouponTable.makeUriForCoupon(coupon.id),
                contentValues,
                null,
                null
        );
        if (count != 1) {
            throw new SQLException("Failed to update coupon state");
        }
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        DebugLog.logMethod();
        DebugLog.logMessage("Y: " + year + ", M: " + month + ", D: " + dayOfMonth);

        coupon.validUntil = Utilities.getLongDate(year, month + 1, dayOfMonth);
        editTextValidUntil.setText(Utilities.getStringDate(coupon.validUntil));

        DebugLog.logMessage("Valid until: " + coupon.validUntil + ", " + editTextValidUntil.getText());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DebugLog.logMethod();
        outState.putInt(Constants.COUPON_FRAGMENT_MODE, mode);
        outState.putParcelable(Constants.COUPON_PARCELABLE, coupon);
        outState.putStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS, merchantSuggestions);
        outState.putStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS, categorySuggestions);
        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            outState.putBoolean(Constants.DATE_PICKER_SHOWING, true);
            outState.putInt(Constants.DATE_PICKER_YEAR, datePickerDialog.getDatePicker().getYear());
            outState.putInt(Constants.DATE_PICKER_MONTH, datePickerDialog.getDatePicker().getMonth());
            outState.putInt(Constants.DATE_PICKER_DAY_OF_MONTH, datePickerDialog.getDatePicker().getDayOfMonth());
        }
    }

    @Override
    public void onDestroyView() {
        DebugLog.logMethod();
        toolbar.setNavigationOnClickListener(null);
        toolbar.setOnMenuItemClickListener(null);

        editTextValidUntil.setOnClickListener(null);
        if (datePickerDialog != null) {
            if (datePickerDialog.isShowing()) {
                datePickerDialog.dismiss();
            }
            datePickerDialog = null;
        }
        buttonCouponState.setOnClickListener(null);

        compositeDisposable.dispose();
        unbinder.unbind();
        super.onDestroyView();
    }
}

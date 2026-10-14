package com.zwyouto.myactivex;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;
import com.zwyouto.main.logind;
import com.zwyouto.mud.BuildConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class resCopy extends AsyncTask<Void, Integer, Long> {
    private final Context mContext;
    private final ProgressDialog mDialog;
    private final File mOutput;

    private long res2sd() {
        String[] list;
        long j = 0;
        try {
            String[] list2 = this.mContext.getResources().getAssets().list(BuildConfig.FLAVOR);
            File file = new File(this.mOutput, "resver.txt");
            if (!Objects.requireNonNull(file.getParentFile()).exists()) {
                file.getParentFile().mkdirs();
            }
            InputStream open = this.mContext.getAssets().open("resver.txt");
            ProgressReportingOutputStream progressReportingOutputStream = new ProgressReportingOutputStream(file);
            j = copy(open, progressReportingOutputStream);
            open.close();
            progressReportingOutputStream.close();
            for (String s : list2) {
                if (!s.equals("images") && !s.equals("sounds") && !s.equals("webkit") && (list = this.mContext.getAssets().list(s)) != null) {
                    publishProgress(0, list.length);
                    for (int i2 = 0; i2 < list.length; i2++) {
                        File file2 = new File(this.mOutput.getAbsolutePath() + File.separator + s + File.separator, list[i2]);
                        if (!file2.getParentFile().exists()) {
                            file2.getParentFile().mkdirs();
                        }
                        publishProgress(i2);
                        InputStream open2 = this.mContext.getAssets().open(s + "/" + list[i2]);
                        ProgressReportingOutputStream progressReportingOutputStream2 = new ProgressReportingOutputStream(file2);
                        j += copy(open2, progressReportingOutputStream2);
                        open2.close();
                        progressReportingOutputStream2.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return j;
    }

    public int copy(InputStream inputStream, OutputStream outputStream) {
        byte[] bArr = new byte[1024];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 1024);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, 1024);
        int i = 0;
        while (true) {
            try {
                try {
                    int read = bufferedInputStream.read(bArr, 0, 1024);
                    if (read == -1) {
                        break;
                    }
                    bufferedOutputStream.write(bArr, 0, read);
                    i += read;
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } finally {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
                try {
                    bufferedInputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
        }
        try {
            bufferedOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return i;
    }

    
    @Override // android.os.AsyncTask
    public Long doInBackground(Void... voidArr) {
        return res2sd();
    }

    
    @Override // android.os.AsyncTask
    public void onPostExecute(Long l) {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        if (isCancelled()) {
            return;
        }
        ((logind) this.mContext).unzipok();
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        if (this.mDialog != null) {
            this.mDialog.setTitle("资源更新中...");
            this.mDialog.setCanceledOnTouchOutside(false);
            this.mDialog.setProgressStyle(1);
            this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {  
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialogInterface) {
                    resCopy.this.cancel(true);
                }
            });
            this.mDialog.show();
        }
    }

    
    @Override // android.os.AsyncTask
    public void onProgressUpdate(Integer... numArr) {
        if (this.mDialog == null) {
            return;
        }
        if (numArr.length <= 1) {
            this.mDialog.setProgress(numArr[0]);
            return;
        }
        this.mDialog.setMax(numArr[1]);
    }

    public resCopy(String out, Context context, boolean replaceAll) {
        this.mContext = context;
        this.mOutput = new File(out);
        if (!this.mOutput.exists() && !this.mOutput.mkdirs()) {
            Toast.makeText(context, "目录创建失败！", Toast.LENGTH_SHORT).show();
        }
        if (context != null) {
            this.mDialog = new ProgressDialog(context);
        } else {
            this.mDialog = null;
        }
    }

    
     
    public static final class ProgressReportingOutputStream extends FileOutputStream {
        @Override // java.io.FileOutputStream, java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            super.write(bArr, i, i2);
        }

        public ProgressReportingOutputStream(File file) throws FileNotFoundException {
            super(file);
        }
    }
}
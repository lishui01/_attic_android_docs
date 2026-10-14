package com.zwyouto.myactivex;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.*;
import android.widget.Toast;
import com.zwyouto.main.logind;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipExtra extends AsyncTask<Void, Integer, Long> {
    private final Context mContext;
    private final ProgressDialog mDialog;
    private final File mInput;
    private final File mOutput;
    private int mProgress = 0;
    private boolean mReplaceAll;

    private int copy(InputStream inputStream, OutputStream outputStream) {
        byte[] bArr = new byte[8192];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 8192);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, 8192);
        int i = 0;
        while (true) {
            try {
                try {
                    int read = bufferedInputStream.read(bArr, 0, 8192);
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

    private long getOriginalSize(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        long j = 0;
        while (entries.hasMoreElements()) {
            ZipEntry nextElement = entries.nextElement();
            if (nextElement.getSize() >= 0) {
                j += nextElement.getSize();
            }
        }
        return j;
    }

    private long unzip() {
        ZipFile zipFile = null;
        long j = 0;
        ZipFile zipFile2 = null;
        try {
            try {
                zipFile = new ZipFile(this.mInput);
            } catch (Throwable th) {
                th = th;
            }
            try {
                publishProgress(0, (int) getOriginalSize(zipFile));
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry nextElement = entries.nextElement();
                    if (!nextElement.isDirectory()) {
                        File file = new File(this.mOutput, nextElement.getName());
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        if (!file.exists() || this.mContext == null || !this.mReplaceAll) {
                        }
                        ProgressReportingOutputStream progressReportingOutputStream = new ProgressReportingOutputStream(file);
                        j += copy(zipFile.getInputStream(nextElement), progressReportingOutputStream);
                        progressReportingOutputStream.close();
                    }
                }
                try {
                    zipFile.close();
                    zipFile2 = zipFile;
                } catch (IOException e) {
                    e.printStackTrace();
                    zipFile2 = zipFile;
                }
            } catch (ZipException e2) {
                try {
                    zipFile2.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                return j;
            } catch (IOException e4) {
                try {
                    zipFile2.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
                return j;
            } catch (Throwable th2) {
                try {
                    zipFile2.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return j;
    }


    @Override // android.os.AsyncTask
    public Long doInBackground(Void... voidArr) {
        return Long.valueOf(unzip());
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
            this.mDialog.setTitle("Extracting");
            this.mDialog.setMessage(this.mInput.getName());
            this.mDialog.setCanceledOnTouchOutside(false);
            this.mDialog.setProgressStyle(1);
            this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialogInterface) {
                    ZipExtra.this.cancel(true);
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

    public ZipExtra(String in, String out, Context context, boolean replaceAll) {
        this.mContext = context;
        this.mReplaceAll = replaceAll;
        this.mInput = new File(in);
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



    public final class ProgressReportingOutputStream extends FileOutputStream {
        @Override // java.io.FileOutputStream, java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            super.write(bArr, i, i2);
            ZipExtra.this.mProgress += i2;
            ZipExtra.this.publishProgress(ZipExtra.this.mProgress);
        }

        public ProgressReportingOutputStream(File file) throws FileNotFoundException {
            super(file);
        }
    }
}
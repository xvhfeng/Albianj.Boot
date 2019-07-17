/*
Copyright (c) 2016, Shanghai YUEWEN Information Technology Co., Ltd. 
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of Shanghai YUEWEN Information Technology Co., Ltd. 
* nor the names of its contributors may be used to endorse or promote products derived from 
* this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY SHANGHAI YUEWEN INFORMATION TECHNOLOGY CO., LTD. 
AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright (c) 2016 著作权由上海阅文信息技术有限公司所有。著作权人保留一切权利。

这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：

* 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以及下述的免责声明。
* 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述的免责声明。
* 未获事前取得书面许可，不得使用柏克莱加州大学或本软件贡献者之名称，来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。

免责声明：本软件是由上海阅文信息技术有限公司及本软件之贡献者以现状提供，本软件包装不负任何明示或默示之担保责任，
包括但不限于就适售性以及特定目的的适用性为默示性担保。加州大学董事会及本软件之贡献者，无论任何条件、无论成因或任何责任主义、
无论此责任为因合约关系、无过失责任主义或因非违约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的任何直接性、间接性、
偶发性、特殊性、惩罚性或任何结果的损害（包括但不限于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
*/
package org.albianj.framework.boot.loader;

import org.albianj.framework.boot.BundleContext;
import org.albianj.framework.boot.servants.CollectServant;
import org.albianj.framework.boot.servants.ConvertServant;
import org.albianj.framework.boot.servants.FileServant;
import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

@BundleSharingTag
public class AlbianSpxClassLoader extends BundleClassLoader {

    private Map<String, TypeFileMetadata> mapTypesInSpxLib;

    protected AlbianSpxClassLoader(String bundleName) {
        super(bundleName);
        mapTypesInSpxLib = new HashMap<>();
    }

    @Override
    public void scanAllClass(BundleContext bctx) {
        super.scanAllClass(bctx);
        loadSpxLib(bctx);
    }

    private void loadSpxLib(BundleContext bctx) {
        File f = findSpxFile(bctx);
        checkSpxFileVersion(f);
        Map<String,TypeFileMetadata> map = unpackSpxFile(f.getName());
        mergerTypeFileMetadata(map,true);
    }

    private File findSpxFile(BundleContext bctx) {
        List<File> files = FileServant.Instance.findFileBySuffix(bctx.getBinFolder(), ".spx");
        if (null == files || 0 == files.size()) { // not found in bin folder
            files = FileServant.Instance.findFileBySuffix(bctx.getLibFolder(), ".spx");
        }

        if (null == files) { // check isNull for find in lib folder
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("LoadSpxLib")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("Found SpxLib Fail")
                    .addMessage("Not Found albian.spx file in bin or lib folder at work folder {0},but the file must exist.please check it.",
                            bctx.getWorkFolder())
                    .build().toLogger();
        }

        if (2 >= files.size()) { // check isNull for find in lib folder
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("LoadSpxLib")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("Found SpxLib Fail")
                    .addMessage("Found {0} albian.spx file in bin or lib folder at work folder {1}.The file must be single.",
                            files.size(), bctx.getWorkFolder())
                    .build().toLogger();
        }

        File spxFile = files.get(0);

        LogServant.Instance.newLogPacketBuilder()
                .forSessionId("LoadSpxLib")
                .atLevel(LoggerLevel.Mark)
                .byCalled(this.getClass())
                .takeBrief("Load SpxFile")
                .addMessage("Load Albian spxFile -> {0}.", spxFile.getName())
                .build().toLogger();

        return spxFile;
    }

    private boolean checkSpxFileVersion(File spxFile) {
        String fname = spxFile.getName();
        int begin = fname.indexOf("Albianj_");
        int end = fname.indexOf(".spx");
        String sVersion = fname.substring(begin + "Albianj_".length(), end);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(spxFile);
            byte[] bVersion = new byte[14];
            fis.read(bVersion);
            String sFVersion = bVersion.toString();
            if (!sFVersion.equalsIgnoreCase(sVersion)) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId("LoadSpxLib")
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("SpxLib Version Fail")
                        .addMessage("albian.spx version is not same.file version -> {0} and packing version -> {1}.",
                                sVersion,bVersion)
                        .build().toLogger();
                return false;
            }
            return true;
        } catch (IOException e) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("LoadSpxLib")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .withCause(e)
                    .takeBrief("SpxLib Version Fail")
                    .addMessage("Open and read Albian.spx -> {0} is fail.",
                            fname)
                    .build().toLogger();
            return false;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogServant.Instance.newLogPacketBuilder()
                            .forSessionId("LoadSpxLib")
                            .atLevel(LoggerLevel.Warn)
                            .byCalled(this.getClass())
                            .takeBrief("SpxLib Version Fail")
                            .addMessage("Close Albian.spx -> {0} is fail.maybe file-handler overflow.",
                                    fname)
                            .build().toLogger();
                }
            }
        }
    }

    private Map<String,TypeFileMetadata> unpackSpxFile(String fname) {
        Map<String,TypeFileMetadata> map = new HashMap<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fname);
            fis.skip(14);// skip version

            ArrayList<byte[]> list = parserSpxFileToBytes(fis);
            if (CollectServant.Instance.isNullOrEmpty(list)) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId("LoadSpxLib")
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("Parser SpxLib Error")
                        .addMessage("Parser Albian.spx -> {0} to bytes array is null or empty,maybe spx-file format is error or not accord with Albian.Loading .",
                                fname)
                        .build().toLogger();
            }
            for (byte[] bs : list) {
                scanSingleJarInSpxLib(fname, "", bs, map);
            }

        } catch (IOException e) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("LoadSpxLib")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .withCause(e)
                    .takeBrief("Parser SpxLib Error")
                    .addMessage("Unpacking or parser Albian.spx -> {0} to jar bytes array is fail.",
                            fname)
                    .build().toLogger();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogServant.Instance.newLogPacketBuilder()
                            .forSessionId("LoadSpxLib")
                            .atLevel(LoggerLevel.Warn)
                            .byCalled(this.getClass())
                            .withCause(e)
                            .takeBrief("Parser SpxLib Fail")
                            .addMessage("Close Albian.spx -> {0} is fail.maybe file-handler overflow.",
                                    fname)
                            .build().toLogger();
                }
            }
        }
        return map;
    }

    private ArrayList<byte[]> parserSpxFileToBytes(FileInputStream fis) throws IOException {
        ArrayList<byte[]> list = null;
            list = new ArrayList<byte[]>();
            byte[] bsize = new byte[4];
            fis.read(bsize);
            long size = ConvertServant.Instance.bytesToInt(bsize, 0);
            for (int i = 0; i < size; i++) {
                byte[] blength = new byte[8];
                fis.read(blength);
                long length = ConvertServant.Instance.bytesToLong(blength, 0);
                byte[] ebytes = new byte[(int) length];
                fis.read(ebytes);
                list.add(ebytes);
            }
            return list;
    }

    public void scanSingleJarInSpxLib(String spxFilename, String jarFilename, byte[] b, Map<String, TypeFileMetadata> map) {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new ByteArrayInputStream(b));
            sacnSingleJarBytes(spxFilename, jarFilename, map, jis);
        } catch (IOException e) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("LoadSpxLib")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .withCause(e)
                    .alwaysThrow(true)
                    .takeBrief("Parser SpxLib Fail")
                    .addMessage("Unpacking or parser Albian.spx -> {0} to jar bytes array is fail.",
                                spxFilename)
                    .build().toLogger();
        } finally {
            try {
                jis.close();
            } catch (IOException e) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId("LoadSpxLib")
                        .atLevel(LoggerLevel.Warn)
                        .byCalled(this.getClass())
                        .withCause(e)
                        .takeBrief("Parser SpxLib Fail")
                        .addMessage("Close Albian.spx -> {0} is fail.maybe file-handler overflow.",
                                spxFilename)
                        .build().toLogger();
            }
        }
    }
}
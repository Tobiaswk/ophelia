// J15_Win32.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"

#ifdef _MANAGED
#pragma managed(push, off)
#endif

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
    return TRUE;
}

std::map<int, std::pair<jobject,jobject> *> sbCtxs;
std::map<int, std::pair<jobject,jobject> *> confCtxs;
static JavaVM* jvm;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	jvm = vm;
	return JNI_VERSION_1_4;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdInit(JNIEnv *, jclass)
{
	return lgLcdInit();
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdDeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdDeInit(JNIEnv *, jclass)
{
	return lgLcdDeInit();
}

DWORD WINAPI j15_configcallback(int connection, const PVOID data)
{
	std::pair<jobject,jobject> *ctx = confCtxs[connection];
	if (!ctx)
		return 0;

	JNIEnv *env;
	if (jvm->AttachCurrentThread((void **)(&env), 0) < 0)
		return 0;

	jclass C_callback = env->FindClass("com/xs0/libs/j15/raw/lgLcdOnConfigureCB");
	if (C_callback == NULL) {
		env->ExceptionClear(); // is this needed? can it even happen? what's the effect? who knows :)
		jvm->DetachCurrentThread();
		return 0;
	}

	jmethodID M_callback = env->GetMethodID(C_callback, "callback", "(ILjava/lang/Object;)I");
	if (M_callback == NULL) {
		env->ExceptionClear();
		jvm->DetachCurrentThread();
		return 0;
	}

	jint result = env->CallIntMethod(ctx->first, M_callback, (jint)connection, ctx->second);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		result = 0;
	}
	jvm->DetachCurrentThread();
	return result;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdConnect
 * Signature: (Lcom/xs0/libs/j15/raw/lgLcdConnectContext;)I
 */

JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdConnect(JNIEnv *env, jclass, jcharArray appFriendlyName, jboolean isPersistent, jboolean isAutostartable, jobject configCallback, jobject configContext, jobject connection)
{
	jclass C_IntHolder = env->FindClass("com/xs0/libs/j15/raw/IntHolder");
	if (C_IntHolder == NULL)
		return -1;

	jfieldID fid_value = env->GetFieldID(C_IntHolder, "value", "I");
	if (fid_value == NULL)
		return -1;

	jchar *nameChars = env->GetCharArrayElements(appFriendlyName, 0);
	if (nameChars == NULL) {
		env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "Couldn't get name chars");
		return -1;
	}

	std::pair<jobject,jobject> *confCtx = 0;
	if (configCallback) {
		jobject ccall = env->NewGlobalRef(configCallback);
		if (ccall == NULL) {
			env->ReleaseCharArrayElements(appFriendlyName, nameChars, JNI_ABORT);
			return -1;
		}

		jobject ccallctx = 0;
		if (configContext) {
			ccallctx = env->NewGlobalRef(configContext);
			if (ccallctx == NULL) {
				env->DeleteGlobalRef(ccall);
				env->ReleaseCharArrayElements(appFriendlyName, nameChars, JNI_ABORT);
				return -1;
			}
		}

		confCtx = new std::pair<jobject,jobject>(ccall, ccallctx);
	}

	lgLcdConnectContext cctx;
	ZeroMemory(&cctx, sizeof(cctx));
	cctx.appFriendlyName = (wchar_t*)nameChars;
	cctx.isAutostartable = isAutostartable==JNI_TRUE ? TRUE : FALSE;
	cctx.isPersistent = isPersistent==JNI_TRUE ? TRUE : FALSE;
	cctx.onConfigure.configCallback = confCtx ? j15_configcallback : 0;
	cctx.onConfigure.configContext = 0;

	int err = lgLcdConnect(&cctx);

	if (err == ERROR_SUCCESS) {
		if (confCtx) {
			confCtxs[cctx.connection] = confCtx;
		}
	} else {
		if (confCtx) {
			if (confCtx->first) env->DeleteGlobalRef(confCtx->first);
			if (confCtx->second) env->DeleteGlobalRef(confCtx->second);
			delete confCtx;
		}
	}

	env->ReleaseCharArrayElements(appFriendlyName, nameChars, JNI_ABORT);

	env->SetIntField(connection, fid_value, cctx.connection);

	return err;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdDisconnect
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdDisconnect(JNIEnv *, jclass, jint conn)
{
	return lgLcdDisconnect(conn);
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdEnumerate
 * Signature: (IILcom/xs0/libs/j15/raw/lgLcdDeviceDesc;)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdEnumerate(JNIEnv *env, jclass, jint connection, jint index, jobject description)
{
	jclass C_lgLcdDeviceDesc = env->FindClass("com/xs0/libs/j15/raw/lgLcdDeviceDesc");
	if (C_lgLcdDeviceDesc == NULL)
		return -1;

	jfieldID fid_Width = env->GetFieldID(C_lgLcdDeviceDesc, "Width", "I");
	if (fid_Width == NULL)
		return -1;

	jfieldID fid_Height = env->GetFieldID(C_lgLcdDeviceDesc, "Height", "I");
	if (fid_Height == NULL)
		return -1;

	jfieldID fid_Bpp = env->GetFieldID(C_lgLcdDeviceDesc, "Bpp", "I");
	if (fid_Bpp == NULL)
		return -1;

	jfieldID fid_NumSoftButtons = env->GetFieldID(C_lgLcdDeviceDesc, "NumSoftButtons", "I");
	if (fid_NumSoftButtons == NULL)
		return -1;

	lgLcdDeviceDesc desc;
	ZeroMemory(&desc, sizeof(desc));
	desc.Width = env->GetIntField(description, fid_Width);
	desc.Height = env->GetIntField(description, fid_Height);
	desc.Bpp = env->GetIntField(description, fid_Bpp);
	desc.NumSoftButtons = env->GetIntField(description, fid_NumSoftButtons);

	int err = lgLcdEnumerate(connection, index, &desc);

	env->SetIntField(description, fid_Width, desc.Width);
	env->SetIntField(description, fid_Height, desc.Height);
	env->SetIntField(description, fid_Bpp, desc.Bpp);
	env->SetIntField(description, fid_NumSoftButtons, desc.NumSoftButtons);

	return err;
}

DWORD WINAPI j15_softbuttoncallback(int device, DWORD dwButtons, const PVOID data)
{
	std::pair<jobject,jobject> *ctx = sbCtxs[device];
	if (!ctx)
		return 0;

	JNIEnv *env;
	if (jvm->AttachCurrentThread((void **)(&env), 0) < 0)
		return 0;

	jclass C_callback = env->FindClass("com/xs0/libs/j15/raw/lgLcdOnSoftButtonsCB");
	if (C_callback == NULL) {
		env->ExceptionClear(); // is this needed? can it even happen? what's the effect? who knows :)
		jvm->DetachCurrentThread();
		return 0;
	}

	jmethodID M_callback = env->GetMethodID(C_callback, "callback", "(IILjava/lang/Object;)I");
	if (M_callback == NULL) {
		env->ExceptionClear();
		jvm->DetachCurrentThread();
		return 0;
	}

	jint result = env->CallIntMethod(ctx->first, M_callback, (jint)device, (jint)dwButtons, ctx->second);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		result = 0;
	}
	jvm->DetachCurrentThread();
	return result;
}

	/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdOpen
 * Signature: (IILcom/xs0/libs/j15/raw/lgLcdOnSoftButtonsCB;Ljava/lang/Object;Lcom/xs0/libs/j15/raw/IntHolder;)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdOpen(JNIEnv *env, jclass, jint connection, jint index, jobject softbuttonsChangedCallback, jobject softbuttonsChangedContext, jobject device)
{
	jclass C_IntHolder = env->FindClass("com/xs0/libs/j15/raw/IntHolder");
	if (C_IntHolder == NULL)
		return -1;

	jfieldID fid_value = env->GetFieldID(C_IntHolder, "value", "I");
	if (fid_value == NULL)
		return -1;

	std::pair<jobject,jobject> *cbctx = 0;
	if (softbuttonsChangedCallback) {
		jobject callbak = env->NewGlobalRef(softbuttonsChangedCallback);
		if (callbak == NULL)
			return -1;

		jobject callbakctx = 0;
		if (softbuttonsChangedContext != NULL) {
			callbakctx = env->NewGlobalRef(softbuttonsChangedContext);
			if (callbakctx == NULL) {
				env->DeleteGlobalRef(callbak);
				return -1;
			}
		}

		cbctx = new std::pair<jobject,jobject>(callbak, callbakctx);
	}

	lgLcdOpenContext ctx;
	ZeroMemory(&ctx, sizeof(ctx));
	ctx.connection = connection;
	ctx.index = index;
	ctx.onSoftbuttonsChanged.softbuttonsChangedCallback = cbctx ? j15_softbuttoncallback : 0;
	ctx.onSoftbuttonsChanged.softbuttonsChangedContext = 0;
	ctx.device = env->GetIntField(device, fid_value);

	int err = lgLcdOpen(&ctx);

	env->SetIntField(device, fid_value, ctx.device);

	if (err == ERROR_SUCCESS) {
		if (cbctx) {
			sbCtxs[ctx.device] = cbctx;
		}
	} else {
		if (cbctx) {
			if (cbctx->first) {
				env->DeleteGlobalRef(cbctx->first);
			}
			if (cbctx->second) {
				env->DeleteGlobalRef(cbctx->second);
			}
			delete cbctx;
		}
	}

	return err;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdClose
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdClose(JNIEnv *env, jclass, jint device)
{
	std::pair<jobject,jobject> *sbctx = sbCtxs[(int)device];
	if (sbctx) {
		sbCtxs.erase((int)device);
		if (sbctx->first) env->DeleteGlobalRef(sbctx->first);
		if (sbctx->second) env->DeleteGlobalRef(sbctx->second);
		delete sbctx;
	}

	return lgLcdClose(device);
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdReadSoftButtons
 * Signature: (ILcom/xs0/libs/j15/raw/IntHolder;)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdReadSoftButtons(JNIEnv *env, jclass, jint device, jobject buttons)
{
	jclass C_IntHolder = env->FindClass("com/xs0/libs/j15/raw/IntHolder");
	if (C_IntHolder == NULL)
		return -1;

	jfieldID fid_value = env->GetFieldID(C_IntHolder, "value", "I");
	if (fid_value == NULL)
		return -1;

	DWORD buts = env->GetIntField(buttons, fid_value);
	int err = lgLcdReadSoftButtons(device, &buts);
	env->SetIntField(buttons, fid_value, buts);
	return err;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdUpdateBitmap
 * Signature: (II[BI)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdUpdateBitmap(JNIEnv *env, jclass, jint device, jint format, jbyteArray data, jint priority)
{
	jbyte* ddata = env->GetByteArrayElements(data, 0);
	if (ddata == NULL)
		return ERROR_ACCESS_DENIED; // or what? :)

	lgLcdBitmap160x43x1 bit;
	bit.hdr.Format = format;
	CopyMemory(bit.pixels, ddata, 160*43);

	int err = lgLcdUpdateBitmap(device, &bit.hdr, priority);

	env->ReleaseByteArrayElements(data, ddata, JNI_ABORT);

	return err;
}

/*
 * Class:     com_xs0_libs_j15_raw_J15Native
 * Method:    _lgLcdSetAsLCDForegroundApp
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_xs0_libs_j15_raw_J15Native__1lgLcdSetAsLCDForegroundApp(JNIEnv *, jclass, jint device, jint foregroundYesNoFlag)
{
	return lgLcdSetAsLCDForegroundApp(device, foregroundYesNoFlag);
}



#ifdef _MANAGED
#pragma managed(pop)
#endif


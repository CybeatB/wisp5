#include <string.h>
#define __int64 int64_t
#include "jni.h"
#include "CRFIDVirus.h"
#include "AES/aes-naive.h"
#include "utils/tools.h"
#include "AES/common/portable.h"

JNIEXPORT jbyteArray JNICALL Java_CRFIDVirus_AES_1decrypt (JNIEnv* env, jclass jc, jbyteArray key, jbyteArray data) {
	AES_CTX ctx;
	uint8_t ckey[16] = {};
	memcpy(ckey, ((*env)->GetByteArrayElements(env, key, NULL)), 16);
	uint8_t cdata[16] = {};
	memcpy(cdata, ((*env)->GetByteArrayElements(env, data, NULL)), 16);
	uint8_t dest[16] = {};

	START_DECRYPT();
	AES_init(&ctx, ckey);
	AES_decrypt(&ctx, cdata, dest);
	END_EXPE();

	jbyteArray retval = (*env)->NewByteArray(env, 16);
	(*env)->SetByteArrayRegion(env, retval, (jsize) 0, (jsize) 16, (jbyte *) dest);

	return retval;
}

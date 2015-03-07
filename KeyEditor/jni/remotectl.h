#ifndef __RKXX_REMOTECTL_H__
#define __RKXX_REMOTECTL_H__
#include <asm/io.h>

typedef enum _RMC_STATE
{
    RMC_IDLE,
    RMC_PRELOAD,
    RMC_USERCODE,
    RMC_GETDATA,
    RMC_SEQUENCE
}eRMC_STATE;


typedef enum _KEY_TYPE
{
    KEY_TYPE_PANEL = 0,
    KEY_TYPE_IR = 1,
    KEY_TYPE_BACK_ADC = 2,
    KEY_TYPE_CNT,
}eKEY_TYPE;

#define MAX_KEYS_CNT 40
/*
struct RKxx_remotectl_platform_data {
	//struct rkxx_remotectl_button *buttons;
	int nbuttons;
	int rep;
	int gpio;
	int active_low;
	int timer;
	int wakeup;
	void (*set_iomux)(void);
};
*/
struct rkxx_remote_key_table{
    int scanCode;
	int keyCode;		
};
/*
struct rkxx_remotectl_button {	
    int usercode;
    int nbuttons;
    struct rkxx_remote_key_table *key_table;
    struct list_head node;
};

struct rkxx_remotectls {
    unsigned char mode;                  // the remote controler 's mode .
                                         // [0]=0 normal mode                          | [0]=1 custom mode 
                                         // [1]=0 don't get an addrcode in custom mode | [1]=1 get an addrcode in custom mode
                                         // [2]=0 normal                               | [2]=1 New addrcode
    int num;                             // the node's number in irctls_list 
    struct list_head irctls_list;        // list of infrared remote controls which were supported.
    rwlock_t ir_rwlock;                  // spin lock
};
*/
/*
  store the keys which infrared remote controller get.
*/
struct bonovo_rc_key {
    int addrCode;
    int scanCode;
};
/*
struct bonovo_rc_buff {
    int head;
    int tail;
    struct bonovo_rc_key * key_table;
    spinlock_t lock;
};
*/
struct bonovo_rc_wkey {
    int addrCode;
    int key_type;
    struct rkxx_remote_key_table key_val;
};
/*
struct bonovo_advance_keys {	
    int usercode;
    int nbuttons;
    int ekey_type;
    struct list_head node;
    struct rkxx_remote_key_table *key_table;
};
*/
struct bonovo_key_addr {
	int ekey_type;
	int addrcode;
};

struct bonovo_advance_key_wbuf {
	struct bonovo_key_addr key_addr;
	struct rkxx_remote_key_table key_table[MAX_KEYS_CNT];
};

#define BONOVO_BUFF_LEN   10  // the max number of the key_table in struct bonovo_rc_buff

#define DEV_NAME      "bonovo_irctl"      // device name
#define CONFIG_DIR_PATH   "/data/"      // the path where the configs of ir were stored in.

#define DEBUG_ON                          // debug flag

#define DEV_MAJOR     235
#define DEV_MINOR	    0

#define IOC_MAGIC     DEV_MAJOR
#define IOCTL_BONOVO_CUSTOM_MODE	  _IO(IOC_MAGIC,   1)                                  // enter custom mode
#define IOCTL_BONOVO_NORMAL_MODE      _IO(IOC_MAGIC,   2)                                    // enter normal mode
#define IOCTL_BONOVO_READ_MODE        _IOR(IOC_MAGIC,  3, int *)                             // read the mode of infrared remote controllor
#define IOCTL_BONOVO_READ_KEY         _IOR(IOC_MAGIC, 10, struct bonovo_rc_key *)            // read one key
#define IOCTL_BONOVO_WRITE_KEY        _IOW(IOC_MAGIC, 11, struct bonovo_rc_wkey *)           // write one key
#define IOCTL_BONOVO_READ_CONFIG      _IOR(IOC_MAGIC, 12, struct rkxx_remote_key_table *)    // read the config corresponding addrcode
#define IOCTL_BONOVO_WRITE_CONFIG     _IOW(IOC_MAGIC, 13, struct rkxx_remote_key_table *)    // write the config corresponding addrcode
#define IOCTL_BONOVO_READ_ADDRCODE    _IOR(IOC_MAGIC, 14, int *)                             // read address code
#define IOCTL_BONOVO_WRITE_ADDRCODE   _IOW(IOC_MAGIC, 15, int *)                             // write address code
#define IOCTL_BONOVO_CLEAR_BUFF       _IO(IOC_MAGIC, 20)                                     // clear buff
#define IOCTL_BONOVO_CLEAR_KEYS       _IO(IOC_MAGIC, 21)                                     // clear all keys
#define IOCTL_BONOVO_READ_ACCURACY    _IO(IOC_MAGIC, 22)
#define IOCTL_BONOVO_WRITE_ACCURACY   _IO(IOC_MAGIC, 23)
#define KEY_VALUE_PAIRS_NUM           51                                                     // pairs
#define IOC_MAXNR                     23
#define MAX_KEYS_CNT                  KEY_VALUE_PAIRS_NUM

#define LOGV(fmt, args...)    ALOGV(fmt, ##args)
#define LOGD(fmt, args...)    ALOGD(fmt, ##args)
#define LOGI(fmt, args...)    ALOGI(fmt, ##args)
#define LOGW(fmt, args...)    ALOGW(fmt, ##args)
#define LOGE(fmt, args...)    ALOGE(fmt, ##args)

#endif


#include "main.h"

typedef struct _CMD_MSG_STRU{
	unsigned int type;
	unsigned char size;
	unsigned char data[COMM_MAX_PARAMNUM];

	pthread_mutex_t msg_lock;
}CMD_MSG_STRU,*PCMD_MSG_STRU;

#define COMMANDSIZE 5
#define COMMANDNUMBER 1

char command_arr_bonovo[COMMANDNUMBER][COMMANDSIZE] = {
	{0xfa,0xfa,0x07,0x00,0x94},							//go to wince
};


void ComEnding(CMD_MSG_STRU* msg)
{
    msg->data[msg->size] = msg->type&0xff;
	msg->data[msg->size+1] = (msg->type>>8)&0xff;

	msg->size += 2;
}

void CommParamCmd(CMD_MSG_STRU* msg, unsigned char* param, unsigned char paramnumber)
{
	int i;
	
	LOGE("paramnumber = %d\n", paramnumber);
	for(i = 0; i < paramnumber; i++)
	{
        msg->data[i + msg->size] = *param++;
		msg->type += msg->data[i + msg->size];
    }
	
	msg->size += paramnumber;

	ComEnding(msg);
}

int CmdTreat(int cmd, CMD_MSG_STRU* msg, unsigned char* param, unsigned char paramnumber)
{	
	int i;
    LOGE("cmd = %d, paramnumber = %d\n", cmd, paramnumber);
	
	for(i = 0; i < COMMANDSIZE; i++)
	{
        msg->data[i] = command_arr_bonovo[cmd][i];
		msg->type += command_arr_bonovo[cmd][i];
	}

	msg->size = COMMANDSIZE;
	
    if(paramnumber == 0)
    {
        CommParamCmd(msg, NULL, paramnumber);
    }
    else
    {
        CommParamCmd(msg, param, paramnumber);
    }
    return TRUE;
}


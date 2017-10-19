#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define global_clock 200

int triggerCheck (double samples[10], char type, double threshold ) {
    double before = (samples[0] + samples[1] + samples[2] + samples[3] + samples[4])/5.0;
    double after = (samples[5] + samples[6] + samples[7] + samples[8] + samples[9])/5.0;
    switch(type) {
        case 'R':
            if (threshold>before && threshold<after) {
                return 1;
            } else {
                return 0;
            }
            break;
        case 'F':
            if (threshold<before && threshold>after) {
                return 1;
            } else {
                return 0;
            }
            break;
        case 'L':
            if ((threshold>before && threshold<after) || (threshold<before && threshold>after)) {
                return 1;
            } else {
                return 0;
            }
            break;
        default:
            return 0;
    }
}

int triggering (double samples[10], char mode, char type, double threshold, int clock) {

    switch(mode) {
        case 'S':
            if (triggerCheck(samples, type, threshold)){
                return 3;
            }
            break;
        case 'N':
            return triggerCheck(samples, type, threshold);
            break;
        default:
            if (triggerCheck(samples, type, threshold)) {
                return 1;
            } else if (clock == global_clock) {
                return 2;
            } else {
                return 0;
            }

    }

}

int main (int argc, char* argv[]) {

    double sampleR[10] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    double sampleF[10] = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0};

    if (triggering(sampleR, 'N', 'R', 0.5, 100)) {
        printf("triggered normal, rising at 0.5\n");
    }
    if (!triggering(sampleF, 'N', 'R', 0.5, 100)) {
        printf("not triggered normal, rising at 0.5\n");
    }
    if (triggering(sampleF, 'N', 'F', 0.5, 100)) {
        printf("triggered normal, falling at 0.5\n");
    }
    if (!triggering(sampleR, 'N', 'F', 0.5, 100)) {
        printf("not triggered normal, falling at 0.5\n");
    }
    if (triggering(sampleR, 'N', 'L', 0.5, 100)) {
        printf("triggered normal, level at 0.5\n");
    }
    if (triggering(sampleF, 'N', 'L', 0.5, 100)) {
        printf("triggered normal, level at 0.5\n");
    }
    if (triggering(sampleR, 'S', 'R', 0.5, 100)==3) {
        printf("triggered single, rising at 0.5\n");
    }
    if (triggering(sampleR, 'A', 'F', 0.5, 200)==2) {
        printf("not triggered but auto triggered, falling at 0.5\n");
    }
    if (!triggering(sampleR, 'A', 'F', 0.5, 100)) {
        printf("not triggered auto, falling at 0.5\n");
    }
    if (!triggering(sampleR, 'A', 'L', 1.0, 100)) {
        printf("not triggered auto, level - threshold not met at 0.5\n");
    }
    return 0;
}

import http from 'k6/http';
import { check } from 'k6';

const largeFile = open('../fixtures/large.csv', 'b');

export const options = {
    scenarios: {
        large_file_import: {
            executor: 'constant-vus',
            vus: 5,
            duration: '60s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95) < 5000'],
        checks: ['rate > 0.95'],
    },
};

export default function () {
    const payload = {
        file: http.file(largeFile, 'large.csv', 'text/csv'),
    };

    const res = http.post('http://localhost:8080/api/v1/deals/import', payload);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response has results': (r) => r.body.includes('results'),
    });
}
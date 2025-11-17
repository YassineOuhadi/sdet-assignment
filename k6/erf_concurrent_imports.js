import http from 'k6/http';
import { check, sleep } from 'k6';

const csv1 = open('../fixtures/sample1.csv', 'b');
const csv2 = open('../fixtures/sample2.csv', 'b');

const files = [csv1, csv2];

export const options = {
    scenarios: {
        csv_import_load: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
            gracefulStop: '5s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95) < 500'],
        checks: ['rate > 0.99']
    },
};

export default function () {
    const randomIndex = Math.floor(Math.random() * files.length);
    const csvData = files[randomIndex];

    const filename = `deals_vu${__VU}_iter${__ITER}.csv`;

    const payload = {
        file: http.file(csvData, filename, 'text/csv'),
    };

    const res = http.post('http://localhost:8080/api/v1/deals/import', payload, {
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.5);
}
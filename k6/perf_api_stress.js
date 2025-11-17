import http from 'k6/http';
import { check, sleep } from 'k6';

const endpoints = [
    { url: 'http://deals-app:8080/api/v1/deals', name: 'list_deals' },
    { url: 'http://deals-app:8080/api/v1/deals/D0001', name: 'get_deal' },
    { url: 'http://deals-app:8080/api/v1/deals/health', name: 'health' },
];

export const options = {
    scenarios: {
        stress_read_apis: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '30s', target: 5 },
                { duration: '1m', target: 5 },
                { duration: '30s', target: 5 },
                { duration: '30s', target: 0 },
            ],
            gracefulStop: '10s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95) < 500'],
        http_req_failed: ['rate < 0.01'],
        checks: ['rate > 0.99'],

        'http_req_duration{name:list_deals}': ['p(95) < 800'],
        'http_req_duration{name:get_deal}': ['p(95) < 300'],
        'http_req_duration{name:health}': ['p(99) < 100'],
    },
};

export default function () {
    const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];

    const params = {
        tags: { name: endpoint.name },
    };

    const res = http.get(endpoint.url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.2);
}